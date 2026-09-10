package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AyahAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == NotificationScheduler.ACTION_TRIGGER_AYAH_NOTIFICATION) {
                    AyahNotificationHelper.showRandomAyahNotification(context)
                }
                // Always ensure next alarm is scheduled after trigger or on device reboot
                NotificationScheduler.scheduleNextAlarm(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
