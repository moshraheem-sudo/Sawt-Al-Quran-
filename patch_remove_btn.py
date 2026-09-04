with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'r') as f:
    content = f.read()

import re

old_header = """                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "إغلاق" else "فتح",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }"""

new_header = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("تأكيد الحذف") },
                            text = { Text("هل أنت متأكد من حذف جميع الملفات المحملة لهذا القارئ؟") },
                            confirmButton = {
                                TextButton(onClick = {
                                    itemsForReciter.forEach { item ->
                                        if (item.file.exists()) {
                                            item.file.delete()
                                        }
                                        onItemDelete(item)
                                    }
                                    showDeleteDialog = false
                                }) {
                                    Text("حذف", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("إلغاء")
                                }
                            }
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الكل",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "إغلاق" else "فتح",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }"""

content = content.replace(old_header, new_header)

with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'w') as f:
    f.write(content)
