with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'r') as f:
    content = f.read()

import_lines = "import android.graphics.BitmapFactory\nimport android.content.Context"
content = content.replace("import android.content.Context", import_lines)

old_builder = """        val builder = NotificationCompat.Builder(context, "ayah_audio_channel")
            .setSmallIcon(R.mipmap.ic_launcher)"""

new_builder = """        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        val builder = NotificationCompat.Builder(context, "ayah_audio_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)"""

content = content.replace(old_builder, new_builder)

with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'w') as f:
    f.write(content)
