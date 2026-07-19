package kr.stonecold.myheritage.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: DataRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun backupData() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            val success = repository.backupDatabase()
            _uiState.value = if (success) {
                SettingsUiState.Success("데이터 백업이 성공적으로 완료되었습니다.")
            } else {
                SettingsUiState.Error(Exception("데이터 백업에 실패했습니다. 내부 저장소 상태를 확인해주세요."))
            }
        }
    }

    fun restoreData() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            val success = repository.restoreDatabase()
            _uiState.value = if (success) {
                SettingsUiState.Success("데이터 복원이 완료되었습니다. 앱의 화면을 다시 불러옵니다.")
            } else {
                SettingsUiState.Error(Exception("백업 파일을 찾을 수 없거나 읽을 수 없습니다."))
            }
        }
    }

    fun backupDataToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            val success = repository.backupDatabaseToUri(uri)
            _uiState.value = if (success) {
                SettingsUiState.Success("선택한 경로에 백업 파일이 성공적으로 저장되었습니다.")
            } else {
                SettingsUiState.Error(Exception("백업 파일 내보내기에 실패했습니다."))
            }
        }
    }

    fun restoreDataFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            val success = repository.restoreDatabaseFromUri(uri)
            _uiState.value = if (success) {
                SettingsUiState.Success("선택한 백업 파일로부터 복원이 완료되었습니다.")
            } else {
                SettingsUiState.Error(Exception("선택한 파일로 복원하는 데 실패했습니다. 올바른 백업 파일인지 확인해주세요."))
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                repository.clearDatabase()
                _uiState.value = SettingsUiState.Success("모든 데이터가 초기화되었습니다.")
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e)
            }
        }
    }

    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }
}

sealed interface SettingsUiState {
    object Idle : SettingsUiState
    object Loading : SettingsUiState
    data class Success(val message: String) : SettingsUiState
    data class Error(val throwable: Throwable) : SettingsUiState
}
