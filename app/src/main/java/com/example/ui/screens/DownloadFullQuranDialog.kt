package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import kotlinx.coroutines.withContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AVAILABLE_RECITERS
import com.example.audio.QuranAudioDownloader
import com.example.audio.Reciter

@Composable
fun DownloadFullQuranDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val progress by QuranAudioDownloader.downloadProgress.collectAsStateWithLifecycle()
    
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
    }
    
    AlertDialog(
        onDismissRequest = { if (progress == null) onDismiss() },
        title = {
            Text(
                text = "تحميل المصحف كاملاً",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (progress != null) {
                    val p = progress!!
                    val percentage = (p.currentSurah * 100) / p.totalSurahs
                    
                    Text(
                        text = if (p.isPaused) "تم الإيقاف المؤقت (سورة ${p.currentSurah} من ${p.totalSurahs})" else "جاري التحميل... سورة ${p.currentSurah} من ${p.totalSurahs}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (p.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LinearProgressIndicator(
                        progress = p.currentSurah / p.totalSurahs.toFloat(),
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (p.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (p.isPaused) "التحميل متوقف مؤقتاً. يمكنك استئنافه متى شئت." else "يرجى عدم إغلاق التطبيق حتى يكتمل التنزيل للاستمتاع بالقراءة والتظليل بدون إنترنت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "اختر القارئ لتحميل جميع سور القرآن الكريم للعمل بدون إنترنت:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AVAILABLE_RECITERS) { reciter ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("مكتمل التنزيل", color = Color(0xFF388E3C), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 4.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "تم التنزيل",
                                                tint = Color(0xFF388E3C),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (progress != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { QuranAudioDownloader.cancel() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("إلغاء", fontSize = 13.sp)
                    }
                    if (progress!!.isPaused) {
                        TextButton(
                            onClick = { QuranAudioDownloader.resume(context) },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("استئناف", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                    } else {
                        TextButton(
                            onClick = { QuranAudioDownloader.pause() },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("إيقاف مؤقت", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                    }
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("إخفاء", fontSize = 13.sp)
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("إغلاق")
                }
            }
        }
    )
}
