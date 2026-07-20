package kr.stonecold.myheritage.ui.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.data.IndividualEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditIndividualViewModel(
    private val repository: DataRepository,
    private val personId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditIndividualUiState>(EditIndividualUiState.Loading)
    val uiState: StateFlow<EditIndividualUiState> = _uiState.asStateFlow()

    init {
        loadIndividual()
    }

    private fun loadIndividual() {
        viewModelScope.launch {
            try {
                val allIndividuals = repository.getAllIndividualsDirect()

                if (personId == -1L) {
                    _uiState.value = EditIndividualUiState.Success(
                        name = "",
                        alias = "",
                        gender = 1,
                        contactId = -1L,
                        relation = 0,
                        relativeId = -1L,
                        kind = 0,
                        familyIdSpouse = -1L,
                        familyIdChild = -1L,
                        individuals = allIndividuals,
                        facts = emptyList(),
                        isNew = true
                    )
                } else {
                    val person = repository.getIndividualById(personId)
                    if (person != null) {
                        val facts = repository.getFactsByPossessId("IND_$personId")

                        _uiState.value = EditIndividualUiState.Success(
                            name = person.name,
                            alias = person.alias ?: "",
                            gender = person.gender,
                            contactId = person.contactId,
                            relation = person.relation,
                            relativeId = person.relativeId,
                            kind = person.kind,
                            familyIdSpouse = person.familyIdSpouse,
                            familyIdChild = person.familyIdChild,
                            individuals = allIndividuals.filter { it.id != personId },
                            facts = facts,
                            isNew = false
                        )
                    } else {
                        _uiState.value = EditIndividualUiState.Error(NoSuchElementException("인물을 찾을 수 없습니다."))
                    }
                }
            } catch (e: Exception) {
                _uiState.value = EditIndividualUiState.Error(e)
            }
        }
    }

    fun saveIndividual(
        name: String,
        alias: String,
        gender: Int,
        relativeId: Long,
        relation: Int,
        onSuccess: () -> Unit
    ) {
        val state = uiState.value
        if (state is EditIndividualUiState.Success) {
            viewModelScope.launch {
                val entity = IndividualEntity(
                    id = if (state.isNew) 0L else personId,
                    gender = gender,
                    name = name.trim(),
                    alias = alias.trim().ifEmpty { null },
                    contactId = state.contactId,
                    relation = relation,
                    relativeId = relativeId,
                    kind = state.kind,
                    lastModified = System.currentTimeMillis(),
                    familyIdSpouse = state.familyIdSpouse,
                    familyIdChild = state.familyIdChild
                )

                if (state.isNew) {
                    val allInds = repository.getAllIndividualsDirect()
                    val targetKind = if (allInds.isEmpty()) -1 else state.kind
                    repository.insertIndividual(entity.copy(kind = targetKind))
                } else {
                    repository.updateIndividual(entity)
                }

                onSuccess()
            }
        }
    }
}

sealed interface EditIndividualUiState {
    object Loading : EditIndividualUiState
    data class Error(val throwable: Throwable) : EditIndividualUiState
    data class Success(
        val name: String,
        val alias: String,
        val gender: Int,
        val contactId: Long,
        val relation: Int,
        val relativeId: Long,
        val kind: Int,
        val familyIdSpouse: Long,
        val familyIdChild: Long,
        val individuals: List<IndividualEntity>,
        val facts: List<kr.stonecold.myheritage.data.FactEntity>,
        val isNew: Boolean
    ) : EditIndividualUiState
}
