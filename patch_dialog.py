import re

with open('app/src/main/java/com/example/ui/screens/DownloadFullQuranDialog.kt', 'r') as f:
    content = f.read()

state_code = """    val progress by QuranAudioDownloader.downloadProgress.collectAsStateWithLifecycle()
    
    val downloadedReciters = remember { mutableStateMapOf<String, Boolean>() }
    
    LaunchedEffect(progress) {
        if (progress == null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                for (reciter in AVAILABLE_RECITERS) {
                    val isDownloaded = QuranAudioDownloader.isReciterDownloaded(context, reciter)
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        downloadedReciters[reciter.id] = isDownloaded
                    }
                }
            }
        }
    }"""

content = content.replace('    val progress by QuranAudioDownloader.downloadProgress.collectAsStateWithLifecycle()', state_code)

row_code = """                                Row(
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
                                            imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                                            contentDescription = "تم التنزيل",
                                            tint = Color(0xFF388E3C)
                                        )
                                    }
                                }"""

content = re.sub(r'                                Row\(.*?\}\n                                \}', row_code, content, flags=re.DOTALL)

if 'import androidx.compose.material.icons.Icons' not in content:
    content = content.replace('import androidx.compose.ui.unit.sp', 'import androidx.compose.ui.unit.sp\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.CheckCircle\nimport kotlinx.coroutines.withContext')

with open('app/src/main/java/com/example/ui/screens/DownloadFullQuranDialog.kt', 'w') as f:
    f.write(content)
