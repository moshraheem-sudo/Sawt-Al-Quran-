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
    val isDownloading: Boolean = true
)

object QuranAudioDownloader {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _downloadProgress = MutableStateFlow<FullQuranDownloadProgress?>(null)
    val downloadProgress: StateFlow<FullQuranDownloadProgress?> = _downloadProgress.asStateFlow()

    fun downloadAll(context: Context, reciter: Reciter) {
        if (_downloadProgress.value?.isDownloading == true) return
        val appContext = context.applicationContext
        
        scope.launch {
            try {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(appContext, "بدأ تنزيل القرآن كاملاً بصوت ${reciter.name}...", android.widget.Toast.LENGTH_LONG).show()
                }

                val audioDir = File(appContext.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
                if (!audioDir.exists()) audioDir.mkdirs()

                for (surahId in 1..114) {
                    val surahStr = String.format(Locale.US, "%03d", surahId)
                    val totalAyahs = SURAH_AYAH_COUNTS[surahId - 1]
                    
                    for (ayahId in 1..totalAyahs) {
                        val ayahStr = String.format(Locale.US, "%03d", ayahId)
                        val destFile = File(audioDir, "$surahStr$ayahStr.mp3")
                        
                        _downloadProgress.value = FullQuranDownloadProgress(
                            reciterId = reciter.id,
                            currentSurah = surahId,
                            currentAyah = ayahId,
                            totalAyahsInSurah = totalAyahs
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
                
                _downloadProgress.value = null
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(appContext, "تم تنزيل القرآن كاملاً بصوت ${reciter.name} بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
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
        // Not easily cancellable in this simple implementation unless we check a flag or cancel the job, 
        // but for now, we just let it run.
    }
}
