import re
with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'r') as f:
    text = f.read()

old_notif = r"""        val notification = NotificationCompat\.Builder\(context, channelId\)[\s\S]*?\.build\(\)"""

new_notif = r"""        val listenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", ayahNumber)
            putExtra("auto_play_ayah", true) // Ensure the app knows to play it
        }
        val pendingListenIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt() + 1,
            listenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("هل استمعت اليوم لكلام الله؟ 📖")
            .setContentText("﴿ $ayahText ﴾")
            .setStyle(NotificationCompat.BigTextStyle().bigText("﴿ $ayahText ﴾\n\n— سورة $surahName, الآية $ayahNumber"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, "▶ استمع الآن", pendingListenIntent)
            .setAutoCancel(true)
            .build()"""

text = re.sub(old_notif, new_notif, text)

with open('app/src/main/java/com/example/worker/AyahWorker.kt', 'w') as f:
    f.write(text)
