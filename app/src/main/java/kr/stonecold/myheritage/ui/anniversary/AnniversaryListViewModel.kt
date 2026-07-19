package kr.stonecold.myheritage.ui.anniversary

import android.icu.util.ChineseCalendar
import android.icu.util.Calendar as IcuCalendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.ui.individual.formatLongDate
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class AnniversaryUiItem(
    val id: Long,
    val label: String,
    val originalDateStr: String,
    val thisYearSolarDateStr: String,
    val personName: String,
    val possessId: String,
    val dateType: Int,
    val isRepeated: Int
)

class AnniversaryListViewModel(
    private val repository: DataRepository
) : ViewModel() {

    val uiState: StateFlow<List<AnniversaryUiItem>> = combine(
        repository.getAllFactsFlow(),
        repository.getAllIndividuals()
    ) { facts, individuals ->
        val indMap = individuals.associateBy { it.id }
        val resultList = mutableListOf<AnniversaryUiItem>()
        
        facts.forEach { fact ->
            val personId = if (fact.possessId.startsWith("IND_")) {
                fact.possessId.substring(4).toLongOrNull() ?: -1L
            } else -1L
            
            val person = indMap[personId]
            val personName = person?.name ?: "미지정"
            
            // 날짜 추출
            val dateVal = fact.startDate
            val originalDateStr = formatLongDate(dateVal)
            
            var thisYearSolarDateStr = "계산 불가"
            val thisYear = Calendar.getInstance().get(Calendar.YEAR)
            
            // 1. 메인 기념일 계산용 캘린더 구성
            var targetCal: Calendar? = null
            
            if (dateVal in 10000000..99991231) {
                val month = ((dateVal % 10000) / 100).toInt()
                val day = (dateVal % 100).toInt()
                
                targetCal = if (fact.dateType == 1) {
                    getSolarCalOfThisYear(thisYear, month, day, true)
                } else {
                    getSolarCalOfThisYear(thisYear, month, day, false)
                }
            } else {
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateVal
                val month = cal.get(Calendar.MONTH) + 1
                val day = cal.get(Calendar.DAY_OF_MONTH)
                
                targetCal = if (fact.dateType == 1) {
                    getSolarCalOfThisYear(thisYear, month, day, true)
                } else {
                    getSolarCalOfThisYear(thisYear, month, day, false)
                }
            }
            
            if (targetCal != null) {
                val mt = targetCal.get(Calendar.MONTH) + 1
                val dy = targetCal.get(Calendar.DAY_OF_MONTH)
                thisYearSolarDateStr = "${targetCal.get(Calendar.YEAR)}년 ${mt}월 ${dy}일"
            }

            val mainItem = AnniversaryUiItem(
                id = fact.id,
                label = fact.label ?: "기념일",
                originalDateStr = originalDateStr,
                thisYearSolarDateStr = thisYearSolarDateStr,
                personName = personName,
                possessId = fact.possessId,
                dateType = fact.dateType,
                isRepeated = fact.isRepeated
            )
            resultList.add(mainItem)
            
            // 2. 만약 구분 라벨이 "사망" 이면 "제사 (기일 하루 전)" 가상 일정 자동 산출 추가
            if (fact.label?.trim() == "사망") {
                // 원래 기일의 하루 전 계산
                val prevOriginalDateStr = getPrevDateStr(dateVal)
                
                // 올해 양력 기일의 하루 전 계산
                var jesaSolarDateStr = "계산 불가"
                if (targetCal != null) {
                    val jesaCal = targetCal.clone() as Calendar
                    jesaCal.add(Calendar.DAY_OF_MONTH, -1)
                    val jM = jesaCal.get(Calendar.MONTH) + 1
                    val jD = jesaCal.get(Calendar.DAY_OF_MONTH)
                    jesaSolarDateStr = "${jesaCal.get(Calendar.YEAR)}년 ${jM}월 ${jD}일"
                }

                val jesaItem = AnniversaryUiItem(
                    id = fact.id + 100000L, // 가상 ID
                    label = "제사",
                    originalDateStr = prevOriginalDateStr,
                    thisYearSolarDateStr = jesaSolarDateStr,
                    personName = personName,
                    possessId = fact.possessId,
                    dateType = fact.dateType,
                    isRepeated = 0 // 제사는 매년 기일 하루 전에 돌아오므로 무조건 반복(0) 처리
                )
                resultList.add(jesaItem)
            }
        }
        resultList
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getSolarCalOfThisYear(thisYear: Int, month: Int, day: Int, isLunar: Boolean): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

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

    private fun getPrevDateStr(dateVal: Long): String {
        return try {
            val cal = Calendar.getInstance()
            if (dateVal in 10000000..99991231) {
                val y = (dateVal / 10000).toInt()
                val m = ((dateVal % 10000) / 100).toInt()
                val d = (dateVal % 100).toInt()
                cal.set(y, m - 1, d)
            } else {
                cal.timeInMillis = dateVal
            }
            cal.add(Calendar.DAY_OF_MONTH, -1)
            val yr = cal.get(Calendar.YEAR)
            val mt = cal.get(Calendar.MONTH) + 1
            val dy = cal.get(Calendar.DAY_OF_MONTH)
            "${yr}년 ${mt}월 ${dy}일"
        } catch (e: Exception) {
            "하루 전 계산 불가"
        }
    }
}
