package kr.stonecold.myheritage.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainScreenViewModel(private val dataRepository: DataRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MainScreenUiState>(MainScreenUiState.Loading)
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = MainScreenUiState.Loading
            try {
                // 실시간 데이터 흐름 결합
                combine(
                    dataRepository.getAllIndividuals(),
                    dataRepository.getAllFamilies(),
                    dataRepository.getAllNonRepeatedFacts()
                ) { individuals, families, facts ->
                    // 루트 인물 (본인) ID 탐색: kind가 -1인 인물을 먼저 찾고, 없으면 등록 순서가 가장 빠른(ID가 가장 작은) 인물 선택
                    val rootInd = individuals.firstOrNull { it.kind == -1 } ?: individuals.minByOrNull { it.id }
                    val rootId = rootInd?.id ?: -1L
                    val myFamilyId = rootInd?.familyIdSpouse ?: -1L

                    DashboardData(
                        totalIndividuals = individuals.size,
                        totalFamilies = families.size,
                        totalAnniversaries = facts.size,
                        rootIndividualId = rootId,
                        myFamilyId = myFamilyId
                    )
                }
                .catch { throwable ->
                    _uiState.value = MainScreenUiState.Error(throwable)
                }
                .collect { dashboardData ->
                    _uiState.value = MainScreenUiState.Success(dashboardData)
                }
            } catch (e: Exception) {
                _uiState.value = MainScreenUiState.Error(e)
            }
        }
    }
}

data class DashboardData(
    val totalIndividuals: Int,
    val totalFamilies: Int,
    val totalAnniversaries: Int,
    val rootIndividualId: Long,
    val myFamilyId: Long
)

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: DashboardData) : MainScreenUiState
}
