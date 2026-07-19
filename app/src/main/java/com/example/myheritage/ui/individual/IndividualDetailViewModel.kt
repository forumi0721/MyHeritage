package com.example.myheritage.ui.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myheritage.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IndividualDetailViewModel(
    private val repository: DataRepository,
    private val personId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<IndividualDetailUiState>(IndividualDetailUiState.Loading)
    val uiState: StateFlow<IndividualDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetailData()
    }

    private fun loadDetailData() {
        if (personId == -1L) {
            _uiState.value = IndividualDetailUiState.Error(IllegalArgumentException("잘못된 인물 ID입니다."))
            return
        }

        viewModelScope.launch {
            _uiState.value = IndividualDetailUiState.Loading
            try {
                // possess_id 형태로 facts, notes, resources를 쿼리함
                val possessId = "IND_$personId"

                combine(
                    repository.getIndividualByIdFlow(personId),
                    repository.getFactsByPossessIdFlow(possessId),
                    repository.getNotesByPossessId(possessId),
                    repository.getResourcesByPossessId(possessId)
                ) { individual, facts, notes, resources ->
                    if (individual == null) {
                        IndividualDetailUiState.Error(NoSuchElementException("해당 인물을 찾을 수 없습니다."))
                    } else {
                        // 부모 가족 정보나 배우자 가족 정보도 추가 로드
                        val childFamily = if (individual.familyIdChild != -1L) repository.getFamilyById(individual.familyIdChild) else null
                        val spouseFamily = if (individual.familyIdSpouse != -1L) repository.getFamilyById(individual.familyIdSpouse) else null

                        IndividualDetailUiState.Success(
                            individual = individual,
                            facts = facts,
                            notes = notes,
                            resources = resources,
                            childFamily = childFamily,
                            spouseFamily = spouseFamily
                        )
                    }
                }
                .catch { t ->
                    _uiState.value = IndividualDetailUiState.Error(t)
                }
                .collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = IndividualDetailUiState.Error(e)
            }
        }
    }

    fun deleteIndividual(onSuccess: () -> Unit) {
        val state = uiState.value
        if (state is IndividualDetailUiState.Success) {
            viewModelScope.launch {
                repository.deleteIndividual(state.individual)
                onSuccess()
            }
        }
    }
}

sealed interface IndividualDetailUiState {
    object Loading : IndividualDetailUiState
    data class Error(val throwable: Throwable) : IndividualDetailUiState
    data class Success(
        val individual: IndividualEntity,
        val facts: List<FactEntity>,
        val notes: List<NoteEntity>,
        val resources: List<ResourceEntity>,
        val childFamily: FamilyEntity?,
        val spouseFamily: FamilyEntity?
    ) : IndividualDetailUiState
}
