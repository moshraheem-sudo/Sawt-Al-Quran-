with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'r') as f:
    content = f.read()

# Replace ic_launcher_foreground with ic_notification
content = content.replace('.setSmallIcon(R.drawable.ic_launcher_foreground)', '.setSmallIcon(R.drawable.ic_notification)')
# Add LargeIcon for even better appearance
import_lines = """import android.graphics.BitmapFactory
import android.app.NotificationChannel"""
content = content.replace('import android.app.NotificationChannel', import_lines)

old_builder = """        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Make sure this exists, or use a default one"""
new_builder = """        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)"""
            
if old_builder in content:
    content = content.replace(old_builder, new_builder)
else:
    print("Builder not found")

with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'w') as f:
    f.write(content)
