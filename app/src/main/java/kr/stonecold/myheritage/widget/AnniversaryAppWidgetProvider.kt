package kr.stonecold.myheritage.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.util.ChineseCalendar
import android.icu.util.Calendar as IcuCalendar
import android.view.View
import android.widget.RemoteViews
import kr.stonecold.myheritage.MainActivity
import kr.stonecold.myheritage.MyHeritageApplication
import kr.stonecold.myheritage.R
import kr.stonecold.myheritage.data.FactEntity
import kr.stonecold.myheritage.data.IndividualEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

data class WidgetAnniversary(
    val dday: String,
    val title: String,
    val date: String
)

data class WidgetResult(
    val primary: WidgetAnniversary,
    val secondary: WidgetAnniversary?
)

open class BaseAnniversaryAppWidgetProvider(
    private val layoutId: Int
) : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val app = context.applicationContext as MyHeritageApplication
        val repository = app.repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // DB 데이터 쿼리
                val facts = repository.getAllFactsFlow().firstOrNull() ?: emptyList()
                val individuals = repository.getAllIndividuals().firstOrNull() ?: emptyList()
                val indMap = individuals.associateBy { it.id }

                val widgetData = calculateUpcomingAnniversaries(facts, indMap)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, layoutId)
                    
                    // 1. 첫 번째 가장 가까운 기념일 셋팅
                    views.setTextViewText(R.id.widget_dday, widgetData.primary.dday)
                    views.setTextViewText(R.id.widget_title, widgetData.primary.title)
                    views.setTextViewText(R.id.widget_date, widgetData.primary.date)

                    // 2. 두 번째 그 다음 기념일 셋팅 (존재 시 노출, 미존재 시 대기상태 처리)
                    if (widgetData.secondary != null) {
                        views.setTextViewText(R.id.widget_next_dday, "[${widgetData.secondary.dday}]")
                        views.setTextViewText(R.id.widget_next_title, widgetData.secondary.title)
                        views.setTextViewText(R.id.widget_next_date, widgetData.secondary.date)
                    } else {
                        views.setTextViewText(R.id.widget_next_dday, "")
                        views.setTextViewText(R.id.widget_next_title, "다음 일정 없음")
                        views.setTextViewText(R.id.widget_next_date, "")
                    }

                    // 터치 시 앱 메인 진입 PendingIntent 결합
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_dday, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_date, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_next_dday, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_next_title, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_next_date, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateUpcomingAnniversaries(
        facts: List<FactEntity>,
        indMap: Map<Long, IndividualEntity>
    ): WidgetResult {
        // 1. 오직 '매년 반복(fact.isRepeated == 0)' 으로 체크된 기념일만 필터링
        val repeatedFacts = facts.filter { fact -> fact.isRepeated == 0 }

        if (repeatedFacts.isEmpty()) {
            return WidgetResult(
                primary = WidgetAnniversary("D-일", "등록된 기념일이 없습니다.", "인물 상세에서 매년반복 일정을 추가하세요."),
                secondary = null
            )
        }

        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        val thisYear = todayCal.get(Calendar.YEAR)

        data class TempCandidate(
            val fact: FactEntity,
            val diffMillis: Long,
            val diffDays: Int,
            val solarDateText: String
        )

        val candidates = mutableListOf<TempCandidate>()

        for (fact in repeatedFacts) {
            val personId = if (fact.possessId.startsWith("IND_")) {
                fact.possessId.substring(4).toLongOrNull() ?: -1L
            } else -1L
            val person = indMap[personId] ?: continue

            val dateVal = fact.startDate
            val isLunar = fact.dateType == 1

            // 월/일 파싱
            val month: Int
            val day: Int
            if (dateVal in 10000000..99991231) {
                month = ((dateVal % 10000) / 100).toInt()
                day = (dateVal % 100).toInt()
            } else {
                val tempCal = Calendar.getInstance()
                tempCal.timeInMillis = dateVal
                month = tempCal.get(Calendar.MONTH) + 1
                day = tempCal.get(Calendar.DAY_OF_MONTH)
            }

            // 올해 기준 양력 후보일 구하기
            var candidateSolarCal = calculateSolarCal(thisYear, month, day, isLunar)

            // 만약 오늘보다 이전이면 내년 후보일로 이월 계산
            if (candidateSolarCal.timeInMillis < todayCal.timeInMillis) {
                candidateSolarCal = calculateSolarCal(thisYear + 1, month, day, isLunar)
            }

            val diffMillis = candidateSolarCal.timeInMillis - todayCal.timeInMillis
            if (diffMillis < 0) continue

            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            val m = candidateSolarCal.get(Calendar.MONTH) + 1
            val d = candidateSolarCal.get(Calendar.DAY_OF_MONTH)
            val solarDateText = "${m}월 ${d}일"

            candidates.add(TempCandidate(fact, diffMillis, diffDays, solarDateText))
        }

        if (candidates.isEmpty()) {
            return WidgetResult(
                primary = WidgetAnniversary("D-일", "다가오는 기념일 없음", "지나지 않은 매년반복 일정이 없습니다."),
                secondary = null
            )
        }

        // 남은 잔여일수(diffMillis) 기준 오름차순 정렬
        candidates.sortBy { it.diffMillis }

        // 1등 산출
        val pCandidate = candidates[0]
        val pPersonId = if (pCandidate.fact.possessId.startsWith("IND_")) {
            pCandidate.fact.possessId.substring(4).toLongOrNull() ?: -1L
        } else -1L
        val pPerson = indMap[pPersonId]
        val pPersonName = pPerson?.name ?: "가족"
        val pDdayStr = if (pCandidate.diffDays == 0) "D-Day" else "D-${pCandidate.diffDays}"
        val pTitleStr = "$pPersonName 님의 ${pCandidate.fact.label ?: "기념일"}"
        val pDateStr = "올해 날짜: ${pCandidate.solarDateText}${if (pCandidate.fact.dateType == 1) " (음력)" else ""}"
        val primary = WidgetAnniversary(pDdayStr, pTitleStr, pDateStr)

        // 2등 산출 (그 다음 기념일)
        var secondary: WidgetAnniversary? = null
        if (candidates.size > 1) {
            val sCandidate = candidates[1]
            val sPersonId = if (sCandidate.fact.possessId.startsWith("IND_")) {
                sCandidate.fact.possessId.substring(4).toLongOrNull() ?: -1L
            } else -1L
            val sPerson = indMap[sPersonId]
            val sPersonName = sPerson?.name ?: "가족"
            val sDdayStr = if (sCandidate.diffDays == 0) "D-Day" else "D-${sCandidate.diffDays}"
            val sTitleStr = "$sPersonName 님의 ${sCandidate.fact.label ?: "기념일"}"
            val sDateStr = "${sCandidate.solarDateText}${if (sCandidate.fact.dateType == 1) " (음력)" else ""}"
            secondary = WidgetAnniversary(sDdayStr, sTitleStr, sDateStr)
        }

        return WidgetResult(primary, secondary)
    }

    private fun calculateSolarCal(year: Int, month: Int, day: Int, isLunar: Boolean): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (isLunar) {
            val cc = ChineseCalendar()
            cc.set(IcuCalendar.EXTENDED_YEAR, year + 2637)
            cc.set(IcuCalendar.MONTH, month - 1)
            cc.set(IcuCalendar.DAY_OF_MONTH, day)
            cc.set(IcuCalendar.IS_LEAP_MONTH, 0)
            cal.timeInMillis = cc.timeInMillis
        } else {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.set(Calendar.DAY_OF_MONTH, day)
        }
        return cal
    }
}

// 라이트 모드용 4x1 위젯 프로바이더
class AnniversaryAppWidgetProvider : BaseAnniversaryAppWidgetProvider(R.layout.anniversary_widget_4x1) {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || 
            intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, AnniversaryAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
