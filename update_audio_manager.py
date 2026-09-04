import re
with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'r') as f:
    text = f.read()

# Add buildNotification function
notification_func = r"""    fun buildNotification(isPlaying: Boolean): android.app.Notification? {
        val context = applicationContext ?: return null
        val surahName = SURAH_NAMES_AR.getOrElse(currentSurahId - 1) { "سورة $currentSurahId" }
        val reciterName = _currentReciter.value.name
        val appName = context.getString(com.example.R.string.app_name)
        
        mediaSession?.setMetadata(
            android.support.v4.media.MediaMetadataCompat.Builder()
                .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE, "سورة $surahName - الآية $currentAyahNumber")
                .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST, "$reciterName • $appName")
                .putLong(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION, -1L)
                .build()
        )
        
        val state = android.support.v4.media.session.PlaybackStateCompat.Builder()
            .setActions(android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY or android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE or android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT or android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP)
            .setState(if (isPlaying) android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING else android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED, android.support.v4.media.session.PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .build()
        mediaSession?.setPlaybackState(state)

        val playPauseIntent = android.app.PendingIntent.getBroadcast(context, 0, android.content.Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_PLAY_PAUSE"), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = android.app.PendingIntent.getBroadcast(context, 1, android.content.Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_NEXT"), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        val prevIntent = android.app.PendingIntent.getBroadcast(context, 2, android.content.Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_PREV"), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = android.app.PendingIntent.getBroadcast(context, 3, android.content.Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_STOP"), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        val playPauseAction = if (isPlaying) {
            androidx.core.app.NotificationCompat.Action(android.R.drawable.ic_media_pause, "إيقاف", playPauseIntent)
        } else {
            androidx.core.app.NotificationCompat.Action(android.R.drawable.ic_media_play, "تشغيل", playPauseIntent)
        }

        val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.icon)

        val openAppIntent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_surah_id", currentSurahId)
            putExtra("open_ayah_number", currentAyahNumber)
        }
        val pendingOpenAppIntent = android.app.PendingIntent.getActivity(context, 100, openAppIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        val builder = androidx.core.app.NotificationCompat.Builder(context, "ayah_audio_channel")
            .setSmallIcon(com.example.R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("سورة $surahName - الآية $currentAyahNumber")
            .setContentText("$reciterName • $appName")
            .setOngoing(isPlaying)
            .setContentIntent(pendingOpenAppIntent)
            .setDeleteIntent(stopIntent)
            .addAction(androidx.core.app.NotificationCompat.Action(android.R.drawable.ic_media_previous, "السابق", prevIntent))
            .addAction(playPauseAction)
            .addAction(androidx.core.app.NotificationCompat.Action(android.R.drawable.ic_media_next, "التالي", nextIntent))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession?.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)

        return builder.build()
    }"""

old_update_notification = r"""    private fun updateNotification\(isPlaying: Boolean\) \{[\s\S]*?notificationManager\?\.notify\(NOTIFICATION_ID, builder\.build\(\)\)\n    \}"""

new_update_notification = r"""    private fun updateNotification(isPlaying: Boolean) {
        val context = applicationContext ?: return
        if (!AyahAudioService.isRunning) {
            val intent = android.content.Intent(context, AyahAudioService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        val notification = buildNotification(isPlaying)
        if (notification != null) {
            notificationManager?.notify(NOTIFICATION_ID, notification)
            AyahAudioService.currentNotification = notification
            AyahAudioService.instance?.startForeground(NOTIFICATION_ID, notification)
        }
    }
""" + notification_func

text = re.sub(old_update_notification, new_update_notification, text)

# add imports for service
with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'w') as f:
    f.write(text)

