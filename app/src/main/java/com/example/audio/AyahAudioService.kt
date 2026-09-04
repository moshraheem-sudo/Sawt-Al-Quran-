package com.example.audio

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

class AyahAudioService : Service() {
    
    companion object {
        var isRunning = false
        var instance: AyahAudioService? = null
        var currentNotification: Notification? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        instance = this
        
        if (intent?.action == "ACTION_STOP_SERVICE") {
            com.example.audio.AyahAudioState.stop()
            stopForeground(true)
            stopSelf()
            isRunning = false
            return START_NOT_STICKY
        }
        
        currentNotification?.let {
            startForeground(1001, it) // Using 1001 as NOTIFICATION_ID (same as AudioPlayerManager)
        }
        
        return START_STICKY
    }
    
    fun updateForegroundState(isPlaying: Boolean, notification: Notification) {
        currentNotification = notification
        if (isPlaying) {
            startForeground(1001, notification)
        } else {
            // Stop foreground but keep the notification so it can be dismissed
            stopForeground(false)
            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(1001, notification)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        instance = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Ensure service keeps running to keep audio & highlighting active
    }
}
