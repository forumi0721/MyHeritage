package kr.stonecold.myheritage.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.util.ChineseCalendar
import android.icu.util.Calendar as IcuCalendar
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

class AnniversaryAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val app = context.applicationContext as MyHeritageApplication
        val repository = app.repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // DB에서 팩트 및 인물 데이터 획득
                val facts = repository.getAllFactsFlow().firstOrNull() ?: emptyList()
                val individuals = repository.getAllIndividuals().firstOrNull() ?: emptyList()
                val indMap = individuals.associateBy { it.id }

                val (ddayText, titleText, dateText) = calculateUpcomingAnniversary(facts, indMap)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.anniversary_widget_4x1)
                    
                    // 뷰 바인딩
                    views.setTextViewText(R.id.widget_dday, ddayText)
                    views.setTextViewText(R.id.widget_title, titleText)
                    views.setTextViewText(R.id.widget_date, dateText)

                    // 터치 시 메인화면 구동 펜딩 인텐트 결합
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

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // 리로드 액션이나 부팅 완료 등 필요한 브로드캐스트 감지 시 위젯 업데이트 강제 기동
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || 
            intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, AnniversaryAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun calculateUpcomingAnniversary(
        facts: List<FactEntity>,
        indMap: Map<Long, IndividualEntity>
    ): Triple<String, String, String> {
        if (facts.isEmpty()) {
            return Triple("D-일", "등록된 기념일이 없습니다.", "인물 관리에서 기념일을 등록해 주세요.")
        }

        val todayCal = Calendar.getInstance()
        // D-Day 계산을 위해 시분초는 0으로 정규화
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)

        val thisYear = todayCal.get(Calendar.YEAR)
        var nearestFact: FactEntity? = null
        var nearestTargetSolarTime = Long.MAX_VALUE
        var nearestDday = -1
        var nearestSolarDateText = ""

        for (fact in facts) {
            val personId = if (fact.possessId.startsWith("IND_")) {
                fact.possessId.substring(4).toLongOrNull() ?: -1L
            } else -1L
            val person = indMap[personId] ?: continue

            val dateVal = fact.startDate
            val isLunar = fact.dateType == 1
            val isRepeated = fact.isRepeated == 1

            // 1. 원래 월/일 정보 획득
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

            // 2. 올해 기준 후보 양력일 계산
            var candidateSolarCal = calculateSolarCal(thisYear, month, day, isLunar)

            // 만약 오늘보다 이전 날짜이고 매년 반복되는 기념일이면, 내년(2027) 후보일로 승격 연산
            if (candidateSolarCal.timeInMillis < todayCal.timeInMillis && isRepeated) {
                candidateSolarCal = calculateSolarCal(thisYear + 1, month, day, isLunar)
            }

            val diffMillis = candidateSolarCal.timeInMillis - todayCal.timeInMillis
            // 이미 지난 단발성 기념일은 스킵
            if (diffMillis < 0) continue

            // 3. 가장 가까운 다가오는 기념일 후보군 갱신
            if (diffMillis < nearestTargetSolarTime) {
                nearestTargetSolarTime = diffMillis
                nearestFact = fact
                
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
                nearestDday = diffDays

                val m = candidateSolarCal.get(Calendar.MONTH) + 1
                val d = candidateSolarCal.get(Calendar.DAY_OF_MONTH)
                nearestSolarDateText = "${candidateSolarCal.get(Calendar.YEAR)}년 ${m}월 ${d}일"
            }
        }

        if (nearestFact == null) {
            return Triple("D-일", "다가오는 기념일 없음", "지나지 않은 기념일이 없습니다.")
        }

        val personId = if (nearestFact.possessId.startsWith("IND_")) {
            nearestFact.possessId.substring(4).toLongOrNull() ?: -1L
        } else -1L
        val person = indMap[personId]
        val personName = person?.name ?: "가족"

        val ddayStr = if (nearestDday == 0) "D-Day" else "D-$nearestDday"
        val labelStr = nearestFact.label ?: "기념일"
        val titleStr = "$personName 님의 $labelStr"
        val dateStr = "올해 날짜: $nearestSolarDateText${if (nearestFact.dateType == 1) " (음력 변환)" else ""}"

        return Triple(ddayStr, titleStr, dateStr)
    }

    private fun calculateSolarCal(year: Int, month: Int, day: Int, isLunar: Boolean): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (isLunar) {
            // ICU ChineseCalendar를 이용한 기원전 2637년 오프셋 계산 음양력 변환
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
