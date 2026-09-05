sed -i '/val pendingRoute by pendingNavigation/i \
                    // Update checking on startup\n\
                    var quranUpdateInfo by remember { mutableStateOf<AppReleaseInfo?>(null) }\n\
                    var showUpdateDialog by remember { mutableStateOf(false) }\n\
\n\
                    LaunchedEffect(Unit) {\n\
                        val result = AppUpdateManager.checkQuranUpdate()\n\
                        val info = result.getOrNull()\n\
                        if (info != null && info.isNewerAvailable && !AppUpdateManager.isQuranVersionPostponed(context, info.tagName)) {\n\
                            quranUpdateInfo = info\n\
                            showUpdateDialog = true\n\
                        }\n\
                    }\n\
\n\
                    if (showUpdateDialog && quranUpdateInfo != null) {\n\
                        AppUpdateDialog(\n\
                            releaseInfo = quranUpdateInfo!!,\n\
                            onDismiss = { showUpdateDialog = false },\n\
                            onPostpone = { \n\
                                AppUpdateManager.postponeQuranVersion(context, quranUpdateInfo!!.tagName)\n\
                                showUpdateDialog = false\n\
                            }\n\
                        )\n\
                    }\n' app/src/main/java/com/example/MainActivity.kt
