import re
with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'r') as f:
    text = f.read()

pattern = r"val notification = NotificationCompat\.Builder\(context, channelId\)[\s\S]*?\.build\(\)"

new_val = """val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("✨ قبس من كتاب الله ✨")
            .setContentText("﴿ $ayahText ﴾")
            .setStyle(NotificationCompat.BigTextStyle().bigText("﴿ $ayahText ﴾\\n\\n— سورة $surahName, الآية $ayahNumber"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()"""

text = re.sub(pattern, new_val.replace("\\n", "\\\\n"), text)

with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'w') as f:
    f.write(text)
