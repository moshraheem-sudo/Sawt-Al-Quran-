import re

with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'r') as f:
    content = f.read()

header = """package com.example.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.R
"""

content = content.replace("package com.example.audio\n\nimport android.media.MediaPlayer", header)

surah_names = """
private val SURAH_NAMES_AR = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
    "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
    "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
    "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
    "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
    "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
    "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
    "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
    "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
    "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
    "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
    "المسد", "الإخلاص", "الفلق", "الناس"
)
"""

content = content.replace('val AVAILABLE_RECITERS', surah_names + '\nval AVAILABLE_RECITERS')

init_code = """
    private var mediaSession: MediaSessionCompat? = null
    private var notificationManager: NotificationManager? = null
    private val NOTIFICATION_ID = 1001
    
    fun initContext(context: android.content.Context) {
        applicationContext = context.applicationContext
        AyahAudioState.manager = this
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ayah_audio_channel", "تشغيل الآيات", NotificationManager.IMPORTANCE_LOW)
            notificationManager?.createNotificationChannel(channel)
        }
        mediaSession = MediaSessionCompat(context, "AyahAudioPlayer")
        mediaSession?.isActive = true
    }
"""

content = content.replace('    fun initContext(context: android.content.Context) {\n        applicationContext = context.applicationContext\n    }', init_code)

update_notif_code = """
    private fun updateNotification(isPlaying: Boolean) {
        val context = applicationContext ?: return
        val surahName = SURAH_NAMES_AR.getOrElse(currentSurahId - 1) { "سورة $currentSurahId" }
        val reciterName = _currentReciter.value.name
        val appName = context.getString(R.string.app_name)
        
        mediaSession?.setMetadata(
            android.support.v4.media.MediaMetadataCompat.Builder()
                .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE, "سورة $surahName - الآية $currentAyahNumber")
                .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST, "$reciterName • $appName")
                .build()
        )

        val playPauseIntent = PendingIntent.getBroadcast(context, 0, Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_PLAY_PAUSE"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getBroadcast(context, 1, Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_NEXT"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val prevIntent = PendingIntent.getBroadcast(context, 2, Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_PREV"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = PendingIntent.getBroadcast(context, 3, Intent(context, AyahNotificationReceiver::class.java).setAction("ACTION_STOP"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "إيقاف", playPauseIntent)
        } else {
            NotificationCompat.Action(android.R.drawable.ic_media_play, "تشغيل", playPauseIntent)
        }

        val builder = NotificationCompat.Builder(context, "ayah_audio_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("سورة $surahName - الآية $currentAyahNumber")
            .setContentText("$reciterName • $appName")
            .setOngoing(isPlaying)
            .setDeleteIntent(stopIntent)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous, "السابق", prevIntent))
            .addAction(playPauseAction)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_next, "التالي", nextIntent))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession?.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }
"""

content = content.replace('    fun setReciter(reciter: Reciter) {', update_notif_code + '\n    fun setReciter(reciter: Reciter) {')


content = content.replace('_isPlaying.value = false\n            }\n        }\n    }', '_isPlaying.value = false\n                updateNotification(false)\n            }\n        }\n    }')
content = content.replace('_isPlaying.value = true\n        } ?: run {', '_isPlaying.value = true\n            updateNotification(true)\n        } ?: run {')

content = content.replace('_currentPlayingAyah.value = Pair(currentSurahId, currentAyahNumber)\n        _isPlaying.value = true', '_currentPlayingAyah.value = Pair(currentSurahId, currentAyahNumber)\n        _isPlaying.value = true\n        updateNotification(true)')

content = content.replace('mediaPlayer = null\n        _isPlaying.value = false', 'mediaPlayer = null\n        _isPlaying.value = false\n        notificationManager?.cancel(NOTIFICATION_ID)')

with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'w') as f:
    f.write(content)
