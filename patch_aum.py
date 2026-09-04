with open('app/src/main/java/com/example/data/remote/AppUpdateManager.kt', 'r') as f:
    content = f.read()

content = content.replace('const val QURAN_CURRENT_VERSION = "v1.40.0"', 'val QURAN_CURRENT_VERSION = "v" + com.example.BuildConfig.VERSION_NAME')
with open('app/src/main/java/com/example/data/remote/AppUpdateManager.kt', 'w') as f:
    f.write(content)
