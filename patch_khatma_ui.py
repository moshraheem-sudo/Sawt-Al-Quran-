import re

with open("app/src/main/java/com/example/ui/screens/KhatmaScreen.kt", "r") as f:
    content = f.read()

# 1. TopAppBar modification
topbar_replacement = """            TopAppBar(
                title = { Text("الختمة القرآنية", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    var showResetDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.RestartAlt, contentDescription = "تصفير الختمة", tint = MaterialTheme.colorScheme.error)
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
            )"""

content = re.sub(r'TopAppBar\(.*?colors = TopAppBarDefaults.topAppBarColors\(.*?\n\s+\)\n\s+\)', topbar_replacement, content, flags=re.DOTALL)

# 2. JuzCard modification
juzcard_sig = "fun JuzCard(juz: JuzUiState, onClick: () -> Unit, onReset: () -> Unit)"
content = content.replace("fun JuzCard(juz: JuzUiState, onClick: () -> Unit)", juzcard_sig)

# Update the call inside LazyColumn
content = content.replace("JuzCard(juz = juz, onClick = { onJuzSelected(juz.juzNumber) })", "JuzCard(juz = juz, onClick = { onJuzSelected(juz.juzNumber) }, onReset = { viewModel.resetJuz(juz.juzNumber) })")


juz_row_replacement = """            Row(
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
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.RestartAlt, contentDescription = "تصفير الجزء", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
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
                                
                Surface("""

content = re.sub(r'            Row\(\n\s+modifier = Modifier\.fillMaxWidth\(\),\n\s+horizontalArrangement = Arrangement\.SpaceBetween,\n\s+verticalAlignment = Alignment\.CenterVertically\n\s+\) \{\n\s+Text\(\n\s+text = getJuzNameArabic\(juz.juzNumber\),\n\s+fontSize = 20.sp,\n\s+fontWeight = FontWeight.Bold,\n\s+color = MaterialTheme.colorScheme.onSurface\n\s+\)\n\s+Surface\(', juz_row_replacement, content, flags=re.DOTALL)


with open("app/src/main/java/com/example/ui/screens/KhatmaScreen.kt", "w") as f:
    f.write(content)
