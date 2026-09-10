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
import com.example.data.local.AyahEntity
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
            val isEnabled = NotificationSettingsManager.areNotificationsEnabled(context)
            if (!isEnabled) return@withContext Result.success()
            
            // Get a random starting ayah
            val initialAyah = repository.getRandomShortAyah(160) ?: repository.getRandomAyah()
            if (initialAyah != null) {
                val surahName = repository.getSurahNameById(initialAyah.surahId) ?: ""
                
                val ayahsList = mutableListOf<AyahEntity>()
                ayahsList.add(initialAyah)

                // If the initial ayah is very short (e.g. less than 50 chars like "الرحمن" or "حم"),
                // include subsequent consecutive ayahs to form a complete, coherent passage (max 4 ayahs / 140 chars)
                var currentAyahNum = initialAyah.ayahNumber + 1
                var totalLen = initialAyah.textUthmani.trim().length
                while (totalLen < 50 && ayahsList.size < 4) {
                    val nextAyah = repository.getAyahByNumber(initialAyah.surahId, currentAyahNum)
                    if (nextAyah != null) {
                        ayahsList.add(nextAyah)
                        totalLen += nextAyah.textUthmani.trim().length
                        currentAyahNum++
                    } else {
                        break
                    }
                }

                showNotification(ayahsList, surahName)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun Int.toArabicNumerals(): String {
        val arabicNumerals = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
    }

    private fun showNotification(ayahs: List<AyahEntity>, surahName: String) {
        if (ayahs.isEmpty()) return
        val firstAyah = ayahs.first()
        val lastAyah = ayahs.last()
        val surahId = firstAyah.surahId

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ayah_of_the_day_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "نفحات من القرآن الكريم",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Format each ayah with its authentic Quranic end symbol ۝ and Arabic numerals, enclosed in ﴿ ﴾
        val versesBody = ayahs.joinToString(" ") { ayah ->
            val cleanText = ayah.textUthmani.trim()
                .replace("*", "")
                .removePrefix("﴿").removeSuffix("﴾")
                .trim()
            "$cleanText ۝${ayah.ayahNumber.toArabicNumerals()}"
        }
        val formattedQuranText = "﴿ $versesBody ﴾"

        // Accurate reference: single ayah vs multiple ayahs
        val ayahRangeStr = if (ayahs.size == 1) {
            "الآية ${firstAyah.ayahNumber.toArabicNumerals()}"
        } else {
            "الآيات ${firstAyah.ayahNumber.toArabicNumerals()}-${lastAyah.ayahNumber.toArabicNumerals()}"
        }
        val fullReference = "سورة $surahName، $ayahRangeStr"

        val readActionLabel = if (ayahs.size == 1) "📖 اقرأ الآية" else "📖 اقرأ الآيات"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", firstAyah.ayahNumber)
        }

        val pendingOpenIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        val listenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", firstAyah.ayahNumber)
            putExtra("auto_play_ayah", true)
        }
        val pendingListenIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt() + 1,
            listenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val readIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", firstAyah.ayahNumber)
        }
        val pendingReadIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt() + 2,
            readIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("نفحات من القرآن الكريم 📖")
            .setContentText("$formattedQuranText - $fullReference")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$formattedQuranText\n\n- $fullReference")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingOpenIntent)
            .addAction(R.drawable.ic_notification, "▶ استمع الآن", pendingListenIntent)
            .addAction(R.drawable.ic_notification, readActionLabel, pendingReadIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }
}
