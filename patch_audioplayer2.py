with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'r') as f:
    content = f.read()

imports = """import com.example.data.local.LastReadManager
"""
if "import com.example.data.local.LastReadManager" not in content:
    content = content.replace("import com.example.R", imports + "import com.example.R")

old_code = """    private fun playCurrent() {
        _currentPlayingAyah.value = Pair(currentSurahId, currentAyahNumber)
        _isPlaying.value = true"""

new_code = """    private fun playCurrent() {
        _currentPlayingAyah.value = Pair(currentSurahId, currentAyahNumber)
        applicationContext?.let { ctx ->
            val surahName = SURAH_NAMES_AR.getOrElse(currentSurahId - 1) { "" }
            LastReadManager.saveLastRead(ctx, currentSurahId, surahName, currentAyahNumber)
        }
        _isPlaying.value = true"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'w') as f:
    f.write(content)
