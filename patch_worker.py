import re

with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'r') as f:
    text = f.read()

# Change the reschedule time
text = text.replace('.setInitialDelay(6, TimeUnit.MINUTES)', '.setInitialDelay(30, TimeUnit.MINUTES)')

# Change the notification style
old_notification_code = r"""        val notification = NotificationCompat\.Builder\(context, channelId\)[\s\S]*?\.build\(\)"""

new_notification_code = """        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("✨ قبس من كتاب الله ✨")
            .setContentText("﴿ " + ayahText + " ﴾")
            .setStyle(NotificationCompat.BigTextStyle().bigText("﴿ " + ayahText + " ﴾\\n\\n— سورة " + surahName + ", الآية " + ayahNumber))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()"""

text = re.sub(old_notification_code, new_notification_code, text)

with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'w') as f:
    f.write(text)

