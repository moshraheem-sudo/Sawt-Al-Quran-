with open('app/src/main/java/com/example/audio/PlayerManager.kt', 'r') as f:
    content = f.read()

old_code = "    fun release() {"
new_code = """    fun closePlayer() {
        controller?.pause()
        _isPlaying.value = false
        _currentTrack.value = null
        _currentTitle.value = ""
        _playlist.value = emptyList()
        appContext?.let { saveState(it) }
    }

    fun release() {"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/audio/PlayerManager.kt', 'w') as f:
    f.write(content)
