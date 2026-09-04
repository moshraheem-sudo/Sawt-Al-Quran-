with open('app/src/main/java/com/example/audio/QuranAudioDownloader.kt', 'r') as f:
    content = f.read()

func = """
    fun isReciterDownloaded(context: Context, reciter: Reciter): Boolean {
        val audioDir = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
        if (!audioDir.exists()) return false
        val count = audioDir.listFiles { file -> file.extension == "mp3" }?.size ?: 0
        return count >= 6236
    }
"""

content = content.replace('fun cancel() {', func + '\n    fun cancel() {')

with open('app/src/main/java/com/example/audio/QuranAudioDownloader.kt', 'w') as f:
    f.write(content)
