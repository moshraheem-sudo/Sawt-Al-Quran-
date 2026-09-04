with open('app/src/main/java/com/example/ui/screens/DownloadFullQuranDialog.kt', 'r') as f:
    content = f.read()

content = content.replace('QuranAudioDownloader.downloadAll(context, reciter)', '''if (downloadedReciters[reciter.id] != true) {
                                                 QuranAudioDownloader.downloadAll(context, reciter)
                                             } else {
                                                 android.widget.Toast.makeText(context, "تم تنزيل هذا القارئ مسبقاً", android.widget.Toast.LENGTH_SHORT).show()
                                             }''')

content = content.replace('color = MaterialTheme.colorScheme.onSurfaceVariant\n                                    )', '''color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (downloadedReciters[reciter.id] == true) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "تم التنزيل",
                                            tint = Color(0xFF388E3C)
                                        )
                                    }''')

with open('app/src/main/java/com/example/ui/screens/DownloadFullQuranDialog.kt', 'w') as f:
    f.write(content)
