package com.example.worker

import android.graphics.BitmapFactory
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.QuranApplication
import com.example.R
import kotlinx.coroutines.Dispatchers
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.data.local.NotificationSettingsManager
import kotlinx.coroutines.withContext

class AyahWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = context.applicationContext as QuranApplication
            val repository = app.repository
            
            // Get a short random ayah (length < 150 chars, but > 15 chars)
            val randomAyah = repository.getRandomShortAyah(150)
            val isEnabled = NotificationSettingsManager.areNotificationsEnabled(context)
            if (isEnabled && randomAyah != null) {
                val surahName = repository.getSurahNameById(randomAyah.surahId) ?: ""
                showNotification(randomAyah.textUthmani, surahName, randomAyah.ayahNumber, randomAyah.surahId)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showNotification(ayahText: String, surahName: String, ayahNumber: Int, surahId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ayah_of_the_day_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعار آية اليوم",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", ayahNumber)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        val listenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", ayahNumber)
            putExtra("auto_play_ayah", true) // Ensure the app knows to play it
        }
        val pendingListenIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt() + 1,
            listenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("هل استمعت اليوم لكلام الله؟ 📖")
            .setContentText("﴿ $ayahText ﴾")
            .setStyle(NotificationCompat.BigTextStyle().bigText("﴿ $ayahText ﴾ - سورة $surahName, الآية $ayahNumber"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, "▶ استمع الآن", pendingListenIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }
}
