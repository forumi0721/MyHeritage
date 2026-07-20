package kr.stonecold.myheritage.ui.tree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.data.IndividualEntity
import kr.stonecold.myheritage.util.AgeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FamilyTreeViewModel(
    private val repository: DataRepository,
    private val rootId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<FamilyTreeUiState>(FamilyTreeUiState.Loading)
    val uiState: StateFlow<FamilyTreeUiState> = _uiState.asStateFlow()

    init {
        loadTreeData()
    }

    fun loadTreeData() {
        if (rootId == -1L) {
            _uiState.value = FamilyTreeUiState.Error(IllegalArgumentException("루트 인물 ID가 잘못되었습니다."))
            return
        }

        viewModelScope.launch {
            _uiState.value = FamilyTreeUiState.Loading
            try {
                val self = repository.getIndividualById(rootId)
                if (self == null) {
                    _uiState.value = FamilyTreeUiState.Error(NoSuchElementException("본인을 찾을 수 없습니다."))
                    return@launch
                }

                // 1. 부모 가족 및 부모/형제자매 로드
                var father: IndividualEntity? = null
                var mother: IndividualEntity? = null
                var siblings = emptyList<IndividualEntity>()

                if (self.familyIdChild != -1L) {
                    val parentFamily = repository.getFamilyById(self.familyIdChild)
                    if (parentFamily != null) {
                        if (parentFamily.husbandId != -1L) {
                            father = repository.getIndividualById(parentFamily.husbandId)
                        }
                        if (parentFamily.wifeId != -1L) {
                            mother = repository.getIndividualById(parentFamily.wifeId)
                        }
                        siblings = repository.getSiblings(self.familyIdChild, self.id)
                    }
                }

                // 2. 배우자 및 자녀 로드
                var spouses = emptyList<IndividualEntity>()
                var children = emptyList<IndividualEntity>()

                val spouseFamilies = repository.getSpouseFamilies(self.id, -1L)
                if (spouseFamilies.isNotEmpty()) {
                    val spouseIds = mutableListOf<Long>()
                    spouseFamilies.forEach { fam ->
                        val spouseId = if (self.gender == 1) fam.wifeId else fam.husbandId
                        if (spouseId != -1L) spouseIds.add(spouseId)
                        val directChildren = repository.getIndividualsByFamilyIdChild(fam.id)
                        children = children + directChildren
                    }
                    spouses = spouseIds.mapNotNull { repository.getIndividualById(it) }
                }

                // 나이 정보 연산을 위해 전체 팩트 로드
                val allFacts = repository.getAllFactsFlow().first()
                val factGroup = allFacts.groupBy { it.possessId }

                val allPersons = listOfNotNull(self, father, mother) + siblings + spouses + children
                val ageMap = allPersons.associate { ind ->
                    val facts = factGroup["IND_${ind.id}"] ?: emptyList()
                    val ageInfo = AgeCalculator.calculateAgeInfo(facts)
                    ind.id to ageInfo.ageText
                }

                _uiState.value = FamilyTreeUiState.Success(
                    self = self,
                    father = father,
                    mother = mother,
                    siblings = siblings,
                    spouses = spouses,
                    children = children,
                    ageMap = ageMap
                )
            } catch (e: Exception) {
                _uiState.value = FamilyTreeUiState.Error(e)
            }
        }
    }
}

sealed interface FamilyTreeUiState {
    object Loading : FamilyTreeUiState
    data class Error(val throwable: Throwable) : FamilyTreeUiState
    data class Success(
        val self: IndividualEntity,
        val father: IndividualEntity?,
        val mother: IndividualEntity?,
        val siblings: List<IndividualEntity>,
        val spouses: List<IndividualEntity>,
        val children: List<IndividualEntity>,
        val ageMap: Map<Long, String?> = emptyMap()
    ) : FamilyTreeUiState
}
