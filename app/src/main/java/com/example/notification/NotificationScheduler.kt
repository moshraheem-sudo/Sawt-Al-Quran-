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

    fun schedule(context: Context) {
        if (!NotificationSettingsManager.areNotificationsEnabled(context)) {
            cancel(context)
            return
        }

        scheduleNextAlarm(context)
        scheduleWorkManagerFallback(context)
    }

    fun scheduleNextAlarm(context: Context) {
        if (!NotificationSettingsManager.areNotificationsEnabled(context)) {
            cancel(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intervalMinutes = NotificationSettingsManager.getIntervalMinutes(context)
        val triggerAtMillis = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L)

        val intent = Intent(context, AyahAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_AYAH_NOTIFICATION
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
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

    private fun scheduleWorkManagerFallback(context: Context) {
        try {
            // WorkManager minimum periodic interval is 15 minutes, serving as a reliable secondary fallback
            val periodicWork = PeriodicWorkRequestBuilder<AyahWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "AyahNotificationWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWork
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, AyahAlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER_AYAH_NOTIFICATION
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            WorkManager.getInstance(context).cancelUniqueWork("AyahNotificationWork")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
