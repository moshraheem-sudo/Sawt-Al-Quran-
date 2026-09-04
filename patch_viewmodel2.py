import re

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'r') as f:
    content = f.read()

old_cancel = """    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _downloadProgress.value = null
        checkIfSurahDownloaded(currentReciter.value.path)
    }"""
new_cancel = """    fun cancelDownload(context: Context) {
        downloadJob?.cancel()
        downloadJob = null
        _downloadProgress.value = null
        checkSurahDownloaded(context)
    }"""
content = content.replace(old_cancel, new_cancel)

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    rcontent = f.read()

rcontent = rcontent.replace('viewModel.cancelDownload()', 'viewModel.cancelDownload(context)')

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(rcontent)
