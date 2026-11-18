package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.NotificationSettingsManager
import com.example.notification.AyahNotificationHelper
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AyahWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        if (!NotificationSettingsManager.areNotificationsEnabled(appContext)) {
            NotificationScheduler.cancel(appContext)
            return@withContext Result.success()
        }

        // Resurrect AlarmManager chain if wiped after reboot/power-off or OEM restriction
        NotificationScheduler.ensureAlarmScheduledIfNeeded(appContext)

        val intervalMs = NotificationSettingsManager.getIntervalMinutes(appContext) * 60_000L
        val lastShown = NotificationSettingsManager.getLastNotificationShownMillis(appContext)
        val now = System.currentTimeMillis()
        val dueThresholdMs = (intervalMs - 90_000L).coerceAtLeast(60_000L)

        if (lastShown == 0L || (now - lastShown) >= dueThresholdMs) {
            AyahNotificationHelper.showRandomAyahNotification(appContext)
        }

        Result.success()
    }
}
