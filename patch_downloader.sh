sed -i 's/private val _downloadProgress = MutableStateFlow<FullQuranDownloadProgress?>(null)/@Volatile\n    private var isCancelled = false\n\n    private val _downloadProgress = MutableStateFlow<FullQuranDownloadProgress?>(null)/g' app/src/main/java/com/example/audio/QuranAudioDownloader.kt

sed -i 's/if (_downloadProgress.value?.isDownloading == true) return/if (_downloadProgress.value?.isDownloading == true) return\n        isCancelled = false/g' app/src/main/java/com/example/audio/QuranAudioDownloader.kt

sed -i 's/for (surahId in 1..114) {/for (surahId in 1..114) {\n                    if (isCancelled) {\n                        withContext(Dispatchers.Main) {\n                            android.widget.Toast.makeText(appContext, "تم إلغاء التنزيل", android.widget.Toast.LENGTH_SHORT).show()\n                        }\n                        break\n                    }/g' app/src/main/java/com/example/audio/QuranAudioDownloader.kt

sed -i 's/for (ayahId in 1..totalAyahs) {/for (ayahId in 1..totalAyahs) {\n                        if (isCancelled) break/g' app/src/main/java/com/example/audio/QuranAudioDownloader.kt

sed -i 's/withContext(Dispatchers.Main) {/if (!isCancelled) {\n                    withContext(Dispatchers.Main) {/g' app/src/main/java/com/example/audio/QuranAudioDownloader.kt
sed -i 's/Toast.makeText(appContext, "تم تنزيل القرآن كاملاً بصوت/Toast.makeText(appContext, "تم تنزيل القرآن كاملاً بصوت/g' app/src/main/java/com/example/audio/QuranAudioDownloader.kt

