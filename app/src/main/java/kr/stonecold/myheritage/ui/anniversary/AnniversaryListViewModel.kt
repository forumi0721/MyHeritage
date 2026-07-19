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
    val dateType: Int
)

class AnniversaryListViewModel(
    private val repository: DataRepository
) : ViewModel() {

    val uiState: StateFlow<List<AnniversaryUiItem>> = combine(
        repository.getAllFactsFlow(),
        repository.getAllIndividuals()
    ) { facts, individuals ->
        val indMap = individuals.associateBy { it.id }
        
        facts.map { fact ->
            // possessId 파싱 (예: "IND_1")
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
            
            if (dateVal in 10000000..99991231) {
                val month = ((dateVal % 10000) / 100).toInt()
                val day = (dateVal % 100).toInt()
                
                if (fact.dateType == 1) {
                    // 음력인 경우: 올해 음력 month월 day일의 양력 날짜 변환 (ICU ChineseCalendar 사용)
                    thisYearSolarDateStr = getSolarDateOfThisYear(thisYear, month, day)
                } else {
                    // 양력인 경우: 올해 연도(2026)와 결합한 날짜
                    thisYearSolarDateStr = "${thisYear}년 ${month}월 ${day}일"
                }
            } else {
                // YYYYMMDD 형태가 아닌 단순 타임스탬프인 경우
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateVal
                val month = cal.get(Calendar.MONTH) + 1
                val day = cal.get(Calendar.DAY_OF_MONTH)
                
                if (fact.dateType == 1) {
                    thisYearSolarDateStr = getSolarDateOfThisYear(thisYear, month, day)
                } else {
                    thisYearSolarDateStr = "${thisYear}년 ${month}월 ${day}일"
                }
            }

            AnniversaryUiItem(
                id = fact.id,
                label = fact.label ?: "기념일",
                originalDateStr = originalDateStr,
                thisYearSolarDateStr = thisYearSolarDateStr,
                personName = personName,
                possessId = fact.possessId,
                dateType = fact.dateType
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ICU ChineseCalendar를 이용한 음력 -> 올해 양력 변환 알고리즘
    private fun getSolarDateOfThisYear(thisYear: Int, lunarMonth: Int, lunarDay: Int): String {
        return try {
            val cc = ChineseCalendar()
            // 음력 연도는 서기 연도에서 대략 2637을 더해 갑자 주기를 지정하나, 
            // ICU ChineseCalendar는 EXTENDED_YEAR 필드에 양력 기준 서기 연도를 바로 주입하여 음력 매핑이 가능함
            cc.set(IcuCalendar.EXTENDED_YEAR, thisYear)
            cc.set(IcuCalendar.MONTH, lunarMonth - 1) // 0-indexed
            cc.set(IcuCalendar.DAY_OF_MONTH, lunarDay)
            cc.set(IcuCalendar.IS_LEAP_MONTH, 0) // 평달 기본

            val timeInMillis = cc.timeInMillis
            val cal = Calendar.getInstance()
            cal.timeInMillis = timeInMillis
            
            val yr = cal.get(Calendar.YEAR)
            val mt = cal.get(Calendar.MONTH) + 1
            val dy = cal.get(Calendar.DAY_OF_MONTH)
            
            "${yr}년 ${mt}월 ${dy}일"
        } catch (e: Exception) {
            "변환 오류"
        }
    }
}
