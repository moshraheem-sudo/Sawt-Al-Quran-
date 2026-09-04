with open('app/src/main/java/com/example/data/remote/AppUpdateManager.kt', 'r') as f:
    content = f.read()

import re

old_catch = """        } catch (e: Exception) {
            onProgress(DownloadState.Error("خطأ أثناء التنزيل: ${e.localizedMessage}"))
        }"""

new_catch = """        } catch (e: kotlinx.coroutines.CancellationException) {
            // Cancelled by user
        } catch (e: Exception) {
            onProgress(DownloadState.Error("خطأ أثناء التنزيل: ${e.localizedMessage ?: e.message}"))
        }"""

content = content.replace(old_catch, new_catch)

with open('app/src/main/java/com/example/data/remote/AppUpdateManager.kt', 'w') as f:
    f.write(content)
