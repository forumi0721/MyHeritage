package kr.stonecold.myheritage.ui.anniversary

import android.icu.util.ChineseCalendar
import android.icu.util.Calendar as IcuCalendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.ui.individual.formatLongDate
import kotlinx.coroutines.flow.*
import java.util.Calendar

enum class AnniversarySortOrder(val label: String) {
    ORIGINAL_ASC("기본 (원래 날짜순)"),
    ORIGINAL_DESC("원래 날짜 역순"),
    UPCOMING_ASC("오늘 기준 다가오는 순"),
    UPCOMING_DESC("오늘 기준 먼 순"),
    THIS_YEAR_SOLAR_ASC("올해 양력 날짜순"),
    THIS_YEAR_SOLAR_DESC("올해 양력 날짜 역순")
}

data class AnniversaryUiItem(
    val id: Long,
    val label: String,
    val originalDateStr: String,
    val thisYearSolarDateStr: String,
    val personName: String,
    val possessId: String,
    val dateType: Int,
    val isRepeated: Int,
    val showInList: Int = 1,
    val originalDateVal: Long = 0L,
    val thisYearSolarMonthDay: Int = 0,
    val upcomingDiffMillis: Long = 0L
)

class AnniversaryListViewModel(
    private val repository: DataRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(AnniversarySortOrder.ORIGINAL_ASC)
    val sortOrder: StateFlow<AnniversarySortOrder> = _sortOrder.asStateFlow()

    private val _showAllFacts = MutableStateFlow(false) // false: 출력 옵션 켜진 것만, true: 전체 출력
    val showAllFacts: StateFlow<Boolean> = _showAllFacts.asStateFlow()

    fun setSortOrder(order: AnniversarySortOrder) {
        _sortOrder.value = order
    }

    fun setShowAllFacts(showAll: Boolean) {
        _showAllFacts.value = showAll
    }

    val uiState: StateFlow<List<AnniversaryUiItem>> = combine(
        repository.getAllFactsFlow(),
        repository.getAllIndividuals(),
        _sortOrder,
        _showAllFacts
    ) { facts, individuals, order, showAll ->
        val indMap = individuals.associateBy { it.id }
        val resultList = mutableListOf<AnniversaryUiItem>()

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val thisYear = todayCal.get(Calendar.YEAR)

        // showAll 이 false 면 showInList == 1 인 팩트만 필터링!
        val filteredFacts = facts.filter { fact -> showAll || fact.showInList == 1 }

        filteredFacts.forEach { fact ->
            val personId = if (fact.possessId.startsWith("IND_")) {
                fact.possessId.substring(4).toLongOrNull() ?: -1L
            } else -1L

            val person = indMap[personId]
            val personName = person?.name ?: "미지정"

            // 날짜 추출
            val dateVal = fact.startDate
            val originalDateStr = formatLongDate(dateVal)

            // 메인 기념일 계산용 캘린더 구성
            val targetCal: Calendar
            val month: Int
            val day: Int

            if (dateVal in 10000000..99991231) {
                month = ((dateVal % 10000) / 100).toInt()
                day = (dateVal % 100).toInt()
            } else {
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateVal
                month = cal.get(Calendar.MONTH) + 1
                day = cal.get(Calendar.DAY_OF_MONTH)
            }

            targetCal = getSolarCalOfThisYear(thisYear, month, day, fact.dateType == 1)

            val mt = targetCal.get(Calendar.MONTH) + 1
            val dy = targetCal.get(Calendar.DAY_OF_MONTH)
            val thisYearSolarDateStr = "${targetCal.get(Calendar.YEAR)}년 ${mt}월 ${dy}일"
            val monthDayVal = mt * 100 + dy

            // 오늘 기준 다가올 잔여 밀리초 계산
            var upcomingCal = targetCal.clone() as Calendar
            if (upcomingCal.timeInMillis < todayCal.timeInMillis) {
                upcomingCal = getSolarCalOfThisYear(thisYear + 1, month, day, fact.dateType == 1)
            }
            val diffMillis = upcomingCal.timeInMillis - todayCal.timeInMillis

            val mainItem = AnniversaryUiItem(
                id = fact.id,
                label = fact.label ?: "기념일",
                originalDateStr = originalDateStr,
                thisYearSolarDateStr = thisYearSolarDateStr,
                personName = personName,
                possessId = fact.possessId,
                dateType = fact.dateType,
                isRepeated = fact.isRepeated,
                showInList = fact.showInList,
                originalDateVal = dateVal,
                thisYearSolarMonthDay = monthDayVal,
                upcomingDiffMillis = diffMillis
            )
            resultList.add(mainItem)
        }

        // 정렬 적용
        when (order) {
            AnniversarySortOrder.ORIGINAL_ASC -> resultList.sortedBy { it.originalDateVal }
            AnniversarySortOrder.ORIGINAL_DESC -> resultList.sortedByDescending { it.originalDateVal }
            AnniversarySortOrder.UPCOMING_ASC -> resultList.sortedBy { it.upcomingDiffMillis }
            AnniversarySortOrder.UPCOMING_DESC -> resultList.sortedByDescending { it.upcomingDiffMillis }
            AnniversarySortOrder.THIS_YEAR_SOLAR_ASC -> resultList.sortedBy { it.thisYearSolarMonthDay }
            AnniversarySortOrder.THIS_YEAR_SOLAR_DESC -> resultList.sortedByDescending { it.thisYearSolarMonthDay }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getSolarCalOfThisYear(thisYear: Int, month: Int, day: Int, isLunar: Boolean): Calendar {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (isLunar) {
            val cc = ChineseCalendar()
            cc.set(IcuCalendar.EXTENDED_YEAR, thisYear + 2637)
            cc.set(IcuCalendar.MONTH, month - 1)
            cc.set(IcuCalendar.DAY_OF_MONTH, day)
            cc.set(IcuCalendar.IS_LEAP_MONTH, 0)
            cal.timeInMillis = cc.timeInMillis
        } else {
            cal.set(Calendar.YEAR, thisYear)
            cal.set(Calendar.MONTH, month - 1)
            cal.set(Calendar.DAY_OF_MONTH, day)
        }
        return cal
    }
}
