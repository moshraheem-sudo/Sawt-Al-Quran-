with open('app/src/main/java/com/example/ui/screens/reciters/ReciterDetailScreen.kt', 'r') as f:
    content = f.read()

import re

# We should use isActive to check cancellation.
old_start = """                for ((index, surahId) in surahList.withIndex()) {
                    val itemKey = "${reciterId}_${style.id}_$surahId"
                    val destFile = File(audioDir, "$itemKey.mp3")

                    if (destFile.exists() && destFile.length() > 0) {
                        continue
                    }

                    while (_batchState.value[key]?.isPaused == true) {
                        kotlinx.coroutines.delay(500)
                    }

                    if (batchJob?.isCancelled == true) break"""

new_start = """                for ((index, surahId) in surahList.withIndex()) {
                    val itemKey = "${reciterId}_${style.id}_$surahId"
                    val destFile = File(audioDir, "$itemKey.mp3")

                    if (destFile.exists() && destFile.length() > 0) {
                        continue
                    }

                    while (_batchState.value[key]?.isPaused == true) {
                        kotlinx.coroutines.delay(500)
                    }

                    if (!kotlinx.coroutines.isActive) break"""

content = content.replace(old_start, new_start)

old_loop = """                            while (input.read(buffer).also { count = it } != -1) {
                                if (batchJob?.isCancelled == true) break
                                while (_batchState.value[key]?.isPaused == true) {
                                    kotlinx.coroutines.delay(500)
                                }"""

new_loop = """                            while (input.read(buffer).also { count = it } != -1) {
                                if (!kotlinx.coroutines.isActive) {
                                    output.flush()
                                    output.close()
                                    input.close()
                                    destFile.delete()
                                    break
                                }
                                while (_batchState.value[key]?.isPaused == true) {
                                    kotlinx.coroutines.delay(500)
                                }"""

content = content.replace(old_loop, new_loop)

old_cancel = """    fun cancelBatchDownload(reciterId: Int, styleId: Int) {
        val key = "${reciterId}_$styleId"
        batchJob?.cancel()
        batchJob = null
        currentReciterId = null
        currentStyleId = null
        _batchState.value = _batchState.value - key
    }"""

new_cancel = """    fun cancelBatchDownload(reciterId: Int, styleId: Int) {
        val key = "${reciterId}_$styleId"
        batchJob?.cancel()
        // do not set to null immediately so isActive becomes false and it breaks
        _batchState.value = _batchState.value - key
    }"""

content = content.replace(old_cancel, new_cancel)

with open('app/src/main/java/com/example/ui/screens/reciters/ReciterDetailScreen.kt', 'w') as f:
    f.write(content)
