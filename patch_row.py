with open('app/src/main/java/com/example/ui/screens/DownloadFullQuranDialog.kt', 'r') as f:
    content = f.read()

target = """                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                             QuranAudioDownloader.downloadAll(context, reciter)
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reciter.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }"""

replacement = """                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                             if (downloadedReciters[reciter.id] != true) {
                                                 QuranAudioDownloader.downloadAll(context, reciter)
                                             } else {
                                                 android.widget.Toast.makeText(context, "تم تنزيل هذا القارئ مسبقاً", android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reciter.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (downloadedReciters[reciter.id] == true) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "تم التنزيل",
                                            tint = Color(0xFF388E3C)
                                        )
                                    }
                                }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DownloadFullQuranDialog.kt', 'w') as f:
    f.write(content)
