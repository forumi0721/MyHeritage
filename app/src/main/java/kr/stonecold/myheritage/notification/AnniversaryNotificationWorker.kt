package kr.stonecold.myheritage.notification

import android.content.Context
import android.icu.util.ChineseCalendar
import android.icu.util.Calendar as IcuCalendar
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kr.stonecold.myheritage.MyHeritageApplication
import kr.stonecold.myheritage.data.FactEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar

class AnniversaryNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as MyHeritageApplication
            val repository = app.repository

            val facts = repository.getAllFactsFlow().first()
            val individuals = repository.getAllIndividualsDirect()
            val indMap = individuals.associateBy { it.id }

            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val thisYear = todayCal.get(Calendar.YEAR)

            val targetFacts = facts.filter { fact -> fact.isRepeated == 0 || fact.label?.trim() == "사망" }

            for (fact in targetFacts) {
                val personId = if (fact.possessId.startsWith("IND_")) {
                    fact.possessId.substring(4).toLongOrNull() ?: -1L
                } else -1L

                val person = indMap[personId] ?: continue
                val personName = if (!person.alias.isNullOrBlank()) "${person.name}(${person.alias})" else person.name
                val dateVal = fact.startDate
                val isLunar = fact.dateType == 1

                val month: Int
                val day: Int
                if (dateVal in 10000000..99991231) {
                    month = ((dateVal % 10000) / 100).toInt()
                    day = (dateVal % 100).toInt()
                } else {
                    val tempCal = Calendar.getInstance().apply { timeInMillis = dateVal }
                    month = tempCal.get(Calendar.MONTH) + 1
                    day = tempCal.get(Calendar.DAY_OF_MONTH)
                }

                // 1. 일반 메인 반복 일정 (isRepeated == 0)
                if (fact.isRepeated == 0) {
                    val candidateSolarCal = calculateSolarCal(thisYear, month, day, isLunar)
                    val diffMillis = candidateSolarCal.timeInMillis - todayCal.timeInMillis
                    val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                    if (diffDays == 0) {
                        val title = "오늘의 기념일 알림"
                        val message = "$personName 님의 ${fact.label ?: "기념일"} 당일입니다."
                        NotificationHelper.sendNotification(applicationContext, fact.id.toInt(), title, message)
                    } else if (diffDays == 1) {
                        val title = "내일의 기념일 알림"
                        val message = "내일은 $personName 님의 ${fact.label ?: "기념일"}입니다."
                        NotificationHelper.sendNotification(applicationContext, (fact.id + 50000L).toInt(), title, message)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun calculateSolarCal(thisYear: Int, month: Int, day: Int, isLunar: Boolean): Calendar {
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
