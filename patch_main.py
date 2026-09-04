with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import_lines = """import androidx.work.OneTimeWorkRequestBuilder
import com.example.data.local.NotificationSettingsManager
"""

if "NotificationSettingsManager" not in content:
    content = content.replace("import androidx.work.PeriodicWorkRequestBuilder", import_lines)

old_work = """        val ayahWorkRequest = PeriodicWorkRequestBuilder<AyahWorker>(20, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AyahNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            ayahWorkRequest
        )"""

new_work = """        if (NotificationSettingsManager.areNotificationsEnabled(this)) {
            val ayahWorkRequest = OneTimeWorkRequestBuilder<AyahWorker>()
                .setInitialDelay(6, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(this).enqueueUniqueWork(
                "AyahNotificationWork",
                androidx.work.ExistingWorkPolicy.KEEP,
                ayahWorkRequest
            )
        }"""

content = content.replace(old_work, new_work)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
