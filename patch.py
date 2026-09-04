import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

replacement = """                // Settings & Tools
                var isToolsExpanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isToolsExpanded,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Offline Download Button"""

content = content.replace('                // Settings & Tools\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    // Offline Download Button', replacement)

replacement2 = """                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(
                        onClick = { isToolsExpanded = !isToolsExpanded },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = if (isToolsExpanded) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = if (isToolsExpanded) "طي الأدوات" else "إظهار الأدوات",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },"""

content = content.replace('''                    IconButton(
                        onClick = { showReciterDialog = true },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },''', '''                    IconButton(
                        onClick = { showReciterDialog = true },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
''' + replacement2)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
