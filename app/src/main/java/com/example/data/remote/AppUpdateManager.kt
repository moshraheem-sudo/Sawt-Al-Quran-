package com.example.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class AppReleaseInfo(
    val appName: String,
    val tagName: String,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val htmlUrl: String,
    val isNewerAvailable: Boolean
)

sealed class DownloadState {
    object Idle : DownloadState()
    object Checking : DownloadState()
    data class Progress(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressPercent: Int,
        val speedMBs: Float
    ) : DownloadState()
    data class Completed(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object AppUpdateManager {
    const val QURAN_APP_NAME = "صوت القرآن"
    val QURAN_CURRENT_VERSION = "v" + com.example.BuildConfig.VERSION_NAME
    const val QURAN_CHECK_API = "https://api.github.com/repos/moshraheem-sudo/Sawt_AL_Quran/releases/latest"
    const val QURAN_RELEASE_PAGE = "https://github.com/moshraheem-sudo/Sawt_AL_Quran/releases/latest"
    const val QURAN_DIRECT_APK = "https://github.com/moshraheem-sudo/Sawt_AL_Quran/releases/latest/download/app-release.apk"

    const val NOUR_APP_NAME = "نور العترة"
    const val NOUR_CURRENT_VERSION = "v1.0.0"
    const val NOUR_CHECK_API = "https://api.github.com/repos/moshraheem-sudo/myapp-updates/releases/latest"
    const val NOUR_RELEASE_PAGE = "https://github.com/moshraheem-sudo/myapp-updates/releases/latest"

    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_POSTPONED_QURAN = "postponed_quran_version"
    private const val KEY_POSTPONED_NOUR = "postponed_nour_version"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1))
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun isQuranVersionPostponed(context: Context, versionTag: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val postponed = prefs.getString(KEY_POSTPONED_QURAN, "") ?: ""
        return postponed == versionTag
    }

    fun postponeQuranVersion(context: Context, versionTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_POSTPONED_QURAN, versionTag).apply()
    }

    fun isNourVersionPostponed(context: Context, versionTag: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val postponed = prefs.getString(KEY_POSTPONED_NOUR, "") ?: ""
        return postponed == versionTag
    }

    fun postponeNourVersion(context: Context, versionTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_POSTPONED_NOUR, versionTag).apply()
    }

    suspend fun checkQuranUpdate(): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(QURAN_CHECK_API)
                .header("User-Agent", "SawtALQuranApp/${com.example.BuildConfig.VERSION_NAME}")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.success(
                        AppReleaseInfo(
                            appName = QURAN_APP_NAME,
                            tagName = QURAN_CURRENT_VERSION,
                            releaseName = "تحديث تطبيق صوت القرآن",
                            releaseNotes = "تحديث تلاوات وتحسين أداء وإصلاحات عامة.",
                            downloadUrl = QURAN_DIRECT_APK,
                            htmlUrl = QURAN_RELEASE_PAGE,
                            isNewerAvailable = false
                        )
                    )
                }

                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val tagName = json.optString("tag_name", QURAN_CURRENT_VERSION)
                val releaseName = json.optString("name", "تحديث صوت القرآن الكريم")
                val releaseNotes = json.optString("body", "يتضمن هذا التحديث تحسينات وإصلاحات عامة لتطبيق صوت القرآن الكريم.").ifBlank {
                    "يتضمن هذا التحديث تحسينات وإصلاحات عامة لتطبيق صوت القرآن الكريم."
                }
                val htmlUrl = json.optString("html_url", QURAN_RELEASE_PAGE)

                var downloadUrl = QURAN_DIRECT_APK
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetUrl = asset.optString("browser_download_url", "")
                        if (assetUrl.contains(".apk", ignoreCase = true)) {
                            downloadUrl = assetUrl
                            break
                        }
                    }
                }

                val isNewer = isVersionNewer(tagName, QURAN_CURRENT_VERSION)

                Result.success(
                    AppReleaseInfo(
                        appName = QURAN_APP_NAME,
                        tagName = tagName,
                        releaseName = releaseName,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        htmlUrl = htmlUrl,
                        isNewerAvailable = isNewer
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkNourUpdate(): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(NOUR_CHECK_API)
                .header("User-Agent", "SawtALQuranApp/${com.example.BuildConfig.VERSION_NAME}")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.success(
                        AppReleaseInfo(
                            appName = NOUR_APP_NAME,
                            tagName = NOUR_CURRENT_VERSION,
                            releaseName = "تحديث تطبيق نور العترة",
                            releaseNotes = "تطبيق نور العترة بالأدعية والتلاوات المباركة.",
                            downloadUrl = "https://github.com/moshraheem-sudo/myapp-updates/releases/latest/download/app-release.apk",
                            htmlUrl = NOUR_RELEASE_PAGE,
                            isNewerAvailable = false
                        )
                    )
                }

                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val tagName = json.optString("tag_name", NOUR_CURRENT_VERSION)
                val releaseName = json.optString("name", "تحديث نور العترة")
                val releaseNotes = json.optString("body", "يتضمن هذا التحديث ميزات ومحتويات جديدة لتطبيق نور العترة.").ifBlank {
                    "يتضمن هذا التحديث ميزات ومحتويات جديدة لتطبيق نور العترة."
                }
                val htmlUrl = json.optString("html_url", NOUR_RELEASE_PAGE)

                var downloadUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetUrl = asset.optString("browser_download_url", "")
                        if (assetUrl.isNotEmpty()) {
                            downloadUrl = assetUrl
                            break
                        }
                    }
                }
                if (downloadUrl.isEmpty()) {
                    downloadUrl = "https://github.com/moshraheem-sudo/myapp-updates/releases/latest/download/app-release.apk"
                }

                val isNewer = isVersionNewer(tagName, NOUR_CURRENT_VERSION)

                Result.success(
                    AppReleaseInfo(
                        appName = NOUR_APP_NAME,
                        tagName = tagName,
                        releaseName = releaseName,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        htmlUrl = htmlUrl,
                        isNewerAvailable = isNewer
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sendNourUpdateNotification(context: Context, releaseInfo: AppReleaseInfo) {
        if (isNourVersionPostponed(context, releaseInfo.tagName)) return

        val channelId = "nour_update_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تحديثات تطبيق نور العترة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات تذكير بوجود تحديثات جديدة لتطبيق نور العترة"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_settings", true)
        }

        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("تحديث جديد لتطبيق نور العترة 🌟")
            .setContentText("يتوفر إصدار جديد (${releaseInfo.tagName}) لتطبيق نور العترة.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("يتوفر إصدار جديد (${releaseInfo.tagName}) لتطبيق نور العترة.\n${releaseInfo.releaseNotes}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(1002, builder.build())
    }

    private fun isVersionNewer(newVer: String, currentVer: String): Boolean {
        val cleanNew = newVer.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = currentVer.trim().removePrefix("v").removePrefix("V")
        if (cleanNew == cleanCurrent) return false

        val newParts = cleanNew.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        if (newParts.isEmpty() || currentParts.isEmpty()) return cleanNew != cleanCurrent

        val maxLen = maxOf(newParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val pNew = newParts.getOrElse(i) { 0 }
            val pCurr = currentParts.getOrElse(i) { 0 }
            if (pNew > pCurr) return true
            if (pNew < pCurr) return false
        }
        return false
    }

    suspend fun downloadApkFile(
        context: Context,
        downloadUrl: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress(DownloadState.Progress(0, 0, 0, 0f))

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "SawtALQuranApp/1.30.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onProgress(DownloadState.Error("فشل التنزيل: رمز ${response.code}"))
                    return@withContext
                }

                val body = response.body
                if (body == null) {
                    onProgress(DownloadState.Error("ملف التنزيل فارغ"))
                    return@withContext
                }

                val totalBytes = body.contentLength()
                val updatesDir = File(context.cacheDir, "updates")
                if (!updatesDir.exists()) updatesDir.mkdirs()

                val apkFile = File(updatesDir, fileName)
                if (apkFile.exists()) apkFile.delete()

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                val startTime = System.currentTimeMillis()
                var lastProgressTime = System.currentTimeMillis()

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastProgressTime > 120 || (totalBytes > 0 && totalBytesRead == totalBytes)) {
                        lastProgressTime = currentTime
                        val timeDiffSec = (currentTime - startTime) / 1000f
                        val speedMBs = if (timeDiffSec > 0) (totalBytesRead / (1024f * 1024f)) / timeDiffSec else 0f
                        val progressPercent = if (totalBytes > 0) ((totalBytesRead * 100) / totalBytes).toInt() else 0

                        onProgress(
                            DownloadState.Progress(
                                downloadedBytes = totalBytesRead,
                                totalBytes = totalBytes,
                                progressPercent = progressPercent,
                                speedMBs = speedMBs
                            )
                        )
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                onProgress(DownloadState.Completed(apkFile))
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Cancelled by user
        } catch (e: Exception) {
            onProgress(DownloadState.Error("خطأ أثناء التنزيل: ${e.localizedMessage ?: e.message}"))
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) return
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
