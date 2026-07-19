package kr.stonecold.myheritage.ui.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.*
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadDetailData() {
        if (personId == -1L) {
            _uiState.value = IndividualDetailUiState.Error(IllegalArgumentException("잘못된 인물 ID입니다."))
            return
        }

        viewModelScope.launch {
            _uiState.value = IndividualDetailUiState.Loading
            try {
                val possessId = "IND_$personId"

                repository.getIndividualByIdFlow(personId).flatMapLatest { individual ->
                    if (individual == null) {
                        flowOf(IndividualDetailUiState.Error(NoSuchElementException("해당 인물을 찾을 수 없습니다.")))
                    } else {
                        val relativeFlow = if (individual.relativeId != -1L) {
                            repository.getIndividualByIdFlow(individual.relativeId)
                        } else {
                            flowOf(null)
                        }

                        combine(
                            relativeFlow,
                            repository.getFactsByPossessIdFlow(possessId),
                            repository.getNotesByPossessId(possessId),
                            repository.getResourcesByPossessId(possessId)
                        ) { relative, facts, notes, resources ->
                            val childFamily = if (individual.familyIdChild != -1L) repository.getFamilyById(individual.familyIdChild) else null
                            val spouseFamily = if (individual.familyIdSpouse != -1L) repository.getFamilyById(individual.familyIdSpouse) else null

                            val relationText = when (individual.relation) {
                                1 -> "부모"
                                2 -> "배우자"
                                3 -> "자녀"
                                4 -> "형제자매"
                                else -> null
                            }

                            IndividualDetailUiState.Success(
                                individual = individual,
                                facts = facts,
                                notes = notes,
                                resources = resources,
                                childFamily = childFamily,
                                spouseFamily = spouseFamily,
                                relativeName = relative?.name,
                                relationText = relationText
                            )
                        }
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
        val spouseFamily: FamilyEntity?,
        val relativeName: String?,
        val relationText: String?
    ) : IndividualDetailUiState
}
