package kr.stonecold.myheritage.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.data.FamilyEntity
import kotlinx.coroutines.flow.*

data class FamilyUiItem(
    val id: Long,
    val husbandName: String?,
    val wifeName: String?,
    val fKindText: String
)

class FamilyListViewModel(
    private val repository: DataRepository
) : ViewModel() {

    val uiState: StateFlow<List<FamilyUiItem>> = combine(
        repository.getAllFamilies(),
        repository.getAllIndividuals()
    ) { families, individuals ->
        val indMap = individuals.associateBy { it.id }
        families.map { family ->
            val husband = indMap[family.husbandId]
            val wife = indMap[family.wifeId]
            
            val kindText = when (family.fKind) {
                1 -> "본가"
                2 -> "외가"
                3 -> "처가"
                4 -> "친가"
                else -> "일반"
            }
            
            FamilyUiItem(
                id = family.id,
                husbandName = husband?.name,
                wifeName = wife?.name,
                fKindText = kindText
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
