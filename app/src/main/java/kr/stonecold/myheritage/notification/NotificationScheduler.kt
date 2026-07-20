package kr.stonecold.myheritage.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_TAG = "AnniversaryNotificationWork"

    fun scheduleNotificationWork(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<AnniversaryNotificationWorker>(12, TimeUnit.HOURS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
