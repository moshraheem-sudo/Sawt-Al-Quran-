package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.NotificationSettingsManager
import com.example.worker.AyahWorker
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    const val ACTION_TRIGGER_AYAH_NOTIFICATION = "com.example.ACTION_TRIGGER_AYAH_NOTIFICATION"
    private const val ALARM_REQUEST_CODE = 4501

    fun schedule(context: Context, forceWorkUpdate: Boolean = false) {
        val appContext = context.applicationContext
        if (!NotificationSettingsManager.areNotificationsEnabled(appContext)) {
            cancel(appContext)
            return
        }

        scheduleNextAlarm(appContext)
        scheduleWorkManagerFallback(appContext, forceUpdate = forceWorkUpdate)
    }

    fun ensureAlarmScheduledIfNeeded(context: Context) {
        val appContext = context.applicationContext
        if (!NotificationSettingsManager.areNotificationsEnabled(appContext)) {
            return
        }

        val intent = Intent(appContext, AyahAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_AYAH_NOTIFICATION
        }
        val existingPendingIntent = PendingIntent.getBroadcast(
            appContext,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val nextAlarmMillis = NotificationSettingsManager.getNextAlarmMillis(appContext)
        val intervalMs = NotificationSettingsManager.getIntervalMinutes(appContext) * 60_000L

        if (existingPendingIntent == null ||
            nextAlarmMillis <= now ||
            (nextAlarmMillis - now) > (intervalMs + 120_000L)
        ) {
            scheduleNextAlarm(appContext)
        }
    }

    fun scheduleNextAlarm(context: Context) {
        val appContext = context.applicationContext
        if (!NotificationSettingsManager.areNotificationsEnabled(appContext)) {
            cancel(appContext)
            return
        }

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intervalMinutes = NotificationSettingsManager.getIntervalMinutes(appContext)
        val triggerAtMillis = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L)
        NotificationSettingsManager.setNextAlarmMillis(appContext, triggerAtMillis)

        val intent = Intent(appContext, AyahAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_AYAH_NOTIFICATION
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun scheduleWorkManagerFallback(context: Context, forceUpdate: Boolean = false) {
        try {
            // WorkManager minimum periodic interval is 15 minutes, serving as a reliable watchdog across reboots
            val periodicWork = PeriodicWorkRequestBuilder<AyahWorker>(15, TimeUnit.MINUTES).build()
            val policy = if (forceUpdate) {
                ExistingPeriodicWorkPolicy.UPDATE
            } else {
                ExistingPeriodicWorkPolicy.KEEP
            }
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                "AyahNotificationWork",
                policy,
                periodicWork
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(context: Context) {
        val appContext = context.applicationContext
        try {
            NotificationSettingsManager.setNextAlarmMillis(appContext, 0L)
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(appContext, AyahAlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER_AYAH_NOTIFICATION
            }
            val pendingIntent = PendingIntent.getBroadcast(
                appContext,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            WorkManager.getInstance(appContext).cancelUniqueWork("AyahNotificationWork")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
