package com.example.audio

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.Locale

data class FullQuranDownloadProgress(
    val reciterId: String,
    val currentSurah: Int,
    val totalSurahs: Int = 114,
    val currentAyah: Int,
    val totalAyahsInSurah: Int,
    val isDownloading: Boolean = true,
    val isPaused: Boolean = false
)

object QuranAudioDownloader {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Volatile
    private var isCancelled = false
    
    @Volatile
    private var isPaused = false
    
    private val _downloadProgress = MutableStateFlow<FullQuranDownloadProgress?>(null)
    val downloadProgress: StateFlow<FullQuranDownloadProgress?> = _downloadProgress.asStateFlow()

    fun downloadAll(context: Context, reciter: Reciter) {
        if (_downloadProgress.value?.isDownloading == true && !_downloadProgress.value!!.isPaused) return
        isCancelled = false
        isPaused = false
        val appContext = context.applicationContext
        
        scope.launch {
            try {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(appContext, "جاري تنزيل القرآن كاملاً بصوت ${reciter.name}...", android.widget.Toast.LENGTH_LONG).show()
                }
                val audioDir = File(appContext.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
                if (!audioDir.exists()) audioDir.mkdirs()
                
                for (surahId in 1..114) {
                    if (isCancelled || isPaused) {
                        break
                    }
                    val surahStr = String.format(Locale.US, "%03d", surahId)
                    val totalAyahs = SURAH_AYAH_COUNTS[surahId - 1]
                    
                    for (ayahId in 1..totalAyahs) {
                        if (isCancelled || isPaused) break
                        
                        val ayahStr = String.format(Locale.US, "%03d", ayahId)
                        val destFile = File(audioDir, "$surahStr$ayahStr.mp3")
                        
                        _downloadProgress.value = FullQuranDownloadProgress(
                            reciterId = reciter.id,
                            currentSurah = surahId,
                            currentAyah = ayahId,
                            totalAyahsInSurah = totalAyahs,
                            isDownloading = true,
                            isPaused = false
                        )
                        
                        if (!destFile.exists() || destFile.length() == 0L) {
                            val urlString = "https://everyayah.com/data/${reciter.path}/$surahStr$ayahStr.mp3"
                            val conn = URL(urlString).openConnection()
                            conn.connect()
                            conn.getInputStream().use { input ->
                                destFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                }
                
                if (isCancelled) {
                    _downloadProgress.value = null
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(appContext, "تم إلغاء التنزيل", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else if (isPaused) {
                    val currentProgress = _downloadProgress.value
                    if (currentProgress != null) {
                        _downloadProgress.value = currentProgress.copy(isPaused = true, isDownloading = false)
                    }
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(appContext, "تم إيقاف التنزيل مؤقتاً", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    _downloadProgress.value = null
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(appContext, "تم تنزيل القرآن كاملاً بصوت ${reciter.name} بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                
            } catch (e: Exception) {
                if (isCancelled || isPaused) return@launch
                _downloadProgress.value = null
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(appContext, "حدث خطأ أثناء التنزيل: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    fun isReciterDownloaded(context: Context, reciter: Reciter): Boolean {
        val audioDir = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
        if (!audioDir.exists()) return false
        val count = audioDir.listFiles { file -> file.extension == "mp3" }?.size ?: 0
        return count >= 6236
    }

    fun cancel() {
        isCancelled = true
        _downloadProgress.value = null
    }
    
    fun pause() {
        isPaused = true
    }
    
    fun resume(context: Context) {
        val current = _downloadProgress.value
        if (current != null && current.isPaused) {
            val reciter = AVAILABLE_RECITERS.find { it.id == current.reciterId }
            if (reciter != null) {
                downloadAll(context, reciter)
            }
        }
    }
}
