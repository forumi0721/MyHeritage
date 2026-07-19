package kr.stonecold.myheritage.ui.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.data.IndividualEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IndividualListViewModel(private val repository: DataRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<IndividualListUiState>(IndividualListUiState.Loading)
    val uiState: StateFlow<IndividualListUiState> = _uiState.asStateFlow()

    init {
        loadIndividuals()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun loadIndividuals() {
        viewModelScope.launch {
            repository.getAllIndividuals()
                .combine(searchQuery) { list, query ->
                    if (query.isBlank()) {
                        list
                    } else {
                        list.filter { it.name.contains(query, ignoreCase = true) || (it.alias?.contains(query, ignoreCase = true) == true) }
                    }
                }
                .catch { t ->
                    _uiState.value = IndividualListUiState.Error(t)
                }
                .collect { filteredList ->
                    _uiState.value = IndividualListUiState.Success(filteredList)
                }
        }
    }
}

sealed interface IndividualListUiState {
    object Loading : IndividualListUiState
    data class Error(val throwable: Throwable) : IndividualListUiState
    data class Success(val list: List<IndividualEntity>) : IndividualListUiState
}
