import re

with open("app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt", "r") as f:
    content = f.read()

nour_vars = """
    // 2. Nour Al-Itrah App Update State
    val nourCurrentVersion = AppUpdateManager.NOUR_CURRENT_VERSION
    var nourUpcomingVersion by remember { mutableStateOf(nourCurrentVersion) }
    var nourUpdateInfo by remember { mutableStateOf<AppReleaseInfo?>(null) }
    var nourUpdateStatus by remember { mutableStateOf(UpdateStatus.IDLE) }
    var nourDownloadProgress by remember { mutableStateOf(0f) }
    var nourDownloadedMB by remember { mutableStateOf(0f) }
    var nourTotalMB by remember { mutableStateOf(0f) }
    var nourSpeedMBs by remember { mutableStateOf(0f) }
    var nourDownloadedApkFile by remember { mutableStateOf<File?>(null) }
    var nourDownloadJob by remember { mutableStateOf<Job?>(null) }
"""

content = content.replace("    // 2. Nour Al-Itrah App Update State", nour_vars)

with open("app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt", "w") as f:
    f.write(content)
