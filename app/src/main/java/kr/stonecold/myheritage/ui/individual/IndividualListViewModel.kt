package kr.stonecold.myheritage.ui.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.data.IndividualEntity
import kr.stonecold.myheritage.util.AgeCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class IndividualListItem(
    val individual: IndividualEntity,
    val ageText: String? = null
)

sealed interface IndividualListUiState {
    object Loading : IndividualListUiState
    data class Error(val throwable: Throwable) : IndividualListUiState
    data class Success(val list: List<IndividualListItem>) : IndividualListUiState
}

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
            combine(
                repository.getAllIndividuals(),
                repository.getAllFactsFlow(),
                searchQuery
            ) { list, allFacts, query ->
                val factMap = allFacts.groupBy { it.possessId }

                val items = list.map { ind ->
                    val personFacts = factMap["IND_${ind.id}"] ?: emptyList()
                    val ageInfo = AgeCalculator.calculateAgeInfo(personFacts)
                    IndividualListItem(
                        individual = ind,
                        ageText = ageInfo.ageText
                    )
                }

                if (query.isBlank()) {
                    items
                } else {
                    items.filter { item ->
                        item.individual.name.contains(query, ignoreCase = true) ||
                                (item.individual.alias?.contains(query, ignoreCase = true) == true)
                    }
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
