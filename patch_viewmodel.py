import re

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'r') as f:
    content = f.read()

# Add downloadJob
content = content.replace('    private val _downloadProgress = MutableStateFlow<SurahDownloadProgress?>(null)', '    private var downloadJob: kotlinx.coroutines.Job? = null\n    private val _downloadProgress = MutableStateFlow<SurahDownloadProgress?>(null)')

# Update downloadSurahAudio
old_download = """
        if (_downloadProgress.value?.isDownloading == true) return

        viewModelScope.launch(Dispatchers.IO) {
"""
new_download = """
        if (_downloadProgress.value?.isDownloading == true) return

        downloadJob = viewModelScope.launch(Dispatchers.IO) {
"""
content = content.replace(old_download, new_download)

old_loop = """                for (i in 1..total) {
                    val ayahStr = String.format(Locale.US, "%03d", i)"""
new_loop = """                for (i in 1..total) {
                    if (!kotlinx.coroutines.isActive) break
                    val ayahStr = String.format(Locale.US, "%03d", i)"""
content = content.replace(old_loop, new_loop)

# Add cancel function
cancel_fun = """
    fun deleteSurahAudio(context: Context) {
"""
new_cancel_fun = """
    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _downloadProgress.value = null
        checkIfSurahDownloaded(currentReciter.value.path)
    }

    fun deleteSurahAudio(context: Context) {
"""
content = content.replace(cancel_fun, new_cancel_fun)

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'w') as f:
    f.write(content)
