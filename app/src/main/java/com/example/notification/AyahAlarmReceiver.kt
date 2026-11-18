package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.HijriCalendarManager
import com.example.data.local.NotificationSettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AyahAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val appContext = context.applicationContext

        val isSystemRestoreEvent = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_USER_UNLOCKED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"

        if (isSystemRestoreEvent) {
            // Immediately re-arm AlarmManager + WorkManager + Hijri periodic sync on main receiver thread
            try {
                HijriCalendarManager.init(appContext)
                if (NotificationSettingsManager.areNotificationsEnabled(appContext)) {
                    NotificationScheduler.schedule(appContext, forceWorkUpdate = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (NotificationSettingsManager.areNotificationsEnabled(appContext)) {
                        val intervalMs = NotificationSettingsManager.getIntervalMinutes(appContext) * 60_000L
                        val lastShown = NotificationSettingsManager.getLastNotificationShownMillis(appContext)
                        val now = System.currentTimeMillis()
                        if (lastShown == 0L || (now - lastShown) >= intervalMs) {
                            AyahNotificationHelper.showRandomAyahNotification(appContext)
                        }
                        NotificationScheduler.ensureAlarmScheduledIfNeeded(appContext)
                    }
                    HijriCalendarManager.refreshFromRemote(appContext, force = false)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (action == NotificationScheduler.ACTION_TRIGGER_AYAH_NOTIFICATION) {
            // Schedule next alarm synchronously first so the chain never breaks even if process is killed
            try {
                NotificationScheduler.scheduleNextAlarm(appContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AyahNotificationHelper.showRandomAyahNotification(appContext)
                    NotificationScheduler.ensureAlarmScheduledIfNeeded(appContext)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
