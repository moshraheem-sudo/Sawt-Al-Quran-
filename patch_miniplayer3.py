import re
with open('app/src/main/java/com/example/ui/screens/reciters/MiniPlayer.kt', 'r') as f:
    content = f.read()

old_code = """                    IconButton(
                        onClick = { PlayerManager.togglePlayPause() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }"""

new_code = """                    IconButton(
                        onClick = { PlayerManager.togglePlayPause() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { PlayerManager.closePlayer() },
                        modifier = Modifier.padding(start = 4.dp).size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }"""

content = content.replace(old_code, new_code)
with open('app/src/main/java/com/example/ui/screens/reciters/MiniPlayer.kt', 'w') as f:
    f.write(content)
