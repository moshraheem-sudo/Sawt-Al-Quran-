package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.khatma.JuzUiState
import com.example.khatma.KhatmaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatmaScreen(
    viewModel: KhatmaViewModel,
    onJuzSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val juzList by viewModel.juzList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
                        TopAppBar(
                title = { Text("الختمة القرآنية", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    var showResetDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "تصفير الختمة", tint = MaterialTheme.colorScheme.error)
                    }
                    if (showResetDialog) {
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            title = { Text("تصفير الختمة", fontWeight = FontWeight.Bold) },
                            text = { Text("هل أنت متأكد من أنك تريد تصفير الختمة والبدء من جديد؟ سيتم مسح تقدم جميع الأجزاء.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.resetKhatma()
                                    showResetDialog = false
                                }) {
                                    Text("نعم، صَفِّر القراءة", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showResetDialog = false }) {
                                    Text("إلغاء")
                                }
                            }
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(juzList, key = { it.juzNumber }) { juz ->
                JuzCard(juz = juz, onClick = { onJuzSelected(juz.juzNumber) }, onReset = { viewModel.resetJuz(juz.juzNumber) })
            }
        }
    }
}

@Composable
fun JuzCard(juz: JuzUiState, onClick: () -> Unit, onReset: () -> Unit) {
    val progress = juz.progressPercent / 100f
    val isCompleted = juz.isCompleted
    val isStarted = juz.progressPercent > 0
    
    val cardColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val progressColor = if (isCompleted) MaterialTheme.colorScheme.primary else if (isStarted) MaterialTheme.colorScheme.tertiary else Color.Transparent
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getJuzNameArabic(juz.juzNumber),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (isStarted) {
                        var showPartResetDialog by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showPartResetDialog = true },
                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "تصفير الجزء", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                        if (showPartResetDialog) {
                            AlertDialog(
                                onDismissRequest = { showPartResetDialog = false },
                                title = { Text("تصفير الجزء", fontWeight = FontWeight.Bold) },
                                text = { Text("هل أنت متأكد أنك تريد تصفير تقدم هذا الجزء والبدء فيه من جديد؟") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onReset()
                                        showPartResetDialog = false
                                    }) {
                                        Text("نعم، صَفِّر", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showPartResetDialog = false }) {
                                        Text("إلغاء")
                                    }
                                }
                            )
                        }
                    }
                }
                                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else if (isStarted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = juz.statusLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) MaterialTheme.colorScheme.onPrimary else if (isStarted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            if (isStarted && !isCompleted) {
                Text(
                    text = "${juz.progressPercent}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

fun getJuzNameArabic(juzNumber: Int): String {
    val names = listOf(
        "الجزء الأول", "الجزء الثاني", "الجزء الثالث", "الجزء الرابع", "الجزء الخامس",
        "الجزء السادس", "الجزء السابع", "الجزء الثامن", "الجزء التاسع", "الجزء العاشر",
        "الجزء الحادي عشر", "الجزء الثاني عشر", "الجزء الثالث عشر", "الجزء الرابع عشر", "الجزء الخامس عشر",
        "الجزء السادس عشر", "الجزء السابع عشر", "الجزء الثامن عشر", "الجزء التاسع عشر", "الجزء العشرون",
        "الجزء الحادي والعشرون", "الجزء الثاني والعشرون", "الجزء الثالث والعشرون", "الجزء الرابع والعشرون", "الجزء الخامس والعشرون",
        "الجزء السادس والعشرون", "الجزء السابع والعشرون", "الجزء الثامن والعشرون", "الجزء التاسع والعشرون", "الجزء الثلاثون"
    )
    return if (juzNumber in 1..30) names[juzNumber - 1] else "الجزء $juzNumber"
}
