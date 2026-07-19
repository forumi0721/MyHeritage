package com.example.myheritage.ui.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myheritage.data.DataRepository
import com.example.myheritage.data.FactEntity
import com.example.myheritage.data.IndividualEntity
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
                // 관계 설정을 위해 등록된 모든 인물 목록 로드
                val allIndividuals = repository.getAllIndividualsDirect()
                
                if (personId == -1L) {
                    _uiState.value = EditIndividualUiState.Success(
                        name = "",
                        alias = "",
                        gender = 1,
                        contactId = -1,
                        relation = 0,
                        relativeId = -1,
                        kind = 0,
                        familyIdSpouse = -1,
                        familyIdChild = -1,
                        birthDateCode = 0L,
                        birthDateType = 0,
                        individuals = allIndividuals,
                        isNew = true
                    )
                } else {
                    val person = repository.getIndividualById(personId)
                    if (person != null) {
                        // 출생 팩트(type = 3) 조회
                        val birthFact = repository.getFactByPossessIdAndType("IND_$personId", 3)
                        
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
                            birthDateCode = birthFact?.startDate ?: 0L,
                            birthDateType = birthFact?.dateType ?: 0,
                            individuals = allIndividuals.filter { it.id != personId }, // 본인은 제외
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
        birthDateCode: Long,
        birthDateType: Int,
        onSuccess: () -> Unit
    ) {
        val state = uiState.value
        if (state is EditIndividualUiState.Success) {
            viewModelScope.launch {
                val entity = IndividualEntity(
                    id = if (state.isNew) 0 else personId,
                    name = name.trim(),
                    alias = alias.trim().ifEmpty { null },
                    gender = gender,
                    contactId = state.contactId,
                    relation = relation,
                    relativeId = relativeId,
                    kind = state.kind,
                    lastModified = System.currentTimeMillis(),
                    familyIdSpouse = state.familyIdSpouse,
                    familyIdChild = state.familyIdChild
                )

                val savedId = if (state.isNew) {
                    repository.insertIndividual(entity)
                } else {
                    repository.updateIndividual(entity)
                    personId
                }

                // 출생일 저장 처리 (birthDateCode > 0 인 경우)
                if (birthDateCode > 0) {
                    val possessId = "IND_$savedId"
                    val existingBirth = repository.getFactByPossessIdAndType(possessId, 3)
                    
                    val birthFact = FactEntity(
                        id = existingBirth?.id ?: 0L,
                        possessId = possessId,
                        type = 3, // 3: 출생
                        label = "출생",
                        startDate = birthDateCode,
                        dateType = birthDateType,
                        isRepeated = 1, // 1: 안함 (생일은 고정 생애이벤트)
                        place = existingBirth?.place,
                        description = existingBirth?.description,
                        lastModified = System.currentTimeMillis()
                    )

                    if (existingBirth != null) {
                        repository.updateFact(birthFact)
                    } else {
                        repository.insertFact(birthFact)
                    }
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
        val birthDateCode: Long,
        val birthDateType: Int,
        val individuals: List<IndividualEntity>,
        val isNew: Boolean
    ) : EditIndividualUiState
}
