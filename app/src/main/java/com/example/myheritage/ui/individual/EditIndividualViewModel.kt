package com.example.myheritage.ui.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myheritage.data.DataRepository
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
        if (personId == -1L) {
            _uiState.value = EditIndividualUiState.Success(
                name = "",
                alias = "",
                gender = 1, // 1: 남성, 2: 여성
                contactId = -1,
                relation = 0,
                relativeId = -1,
                kind = 0,
                familyIdSpouse = -1,
                familyIdChild = -1,
                isNew = true
            )
        } else {
            viewModelScope.launch {
                val person = repository.getIndividualById(personId)
                if (person != null) {
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
                        isNew = false
                    )
                } else {
                    _uiState.value = EditIndividualUiState.Error(NoSuchElementException("인물을 찾을 수 없습니다."))
                }
            }
        }
    }

    fun saveIndividual(
        name: String,
        alias: String,
        gender: Int,
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
                    relation = state.relation,
                    relativeId = state.relativeId,
                    kind = state.kind,
                    lastModified = System.currentTimeMillis(),
                    familyIdSpouse = state.familyIdSpouse,
                    familyIdChild = state.familyIdChild
                )

                if (state.isNew) {
                    repository.insertIndividual(entity)
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
        val isNew: Boolean
    ) : EditIndividualUiState
}
