import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

# Remove state
content = re.sub(r'\s*var showBookmarksSheet by remember \{ mutableStateOf\(false\) \}', '', content)

# Remove Icon Button
icon_btn = """                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(
                        onClick = { showBookmarksSheet = true },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (bookmarks.any { it.surahId == activeSurahId }) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "العلامات المحفوظة",
                            modifier = Modifier.size(18.dp),
                            tint = if (bookmarks.any { it.surahId == activeSurahId }) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }"""
content = content.replace(icon_btn, '')

# Remove bottom sheet
sheet_pattern = re.compile(r'\s*if\s*\(showBookmarksSheet\)\s*\{\s*ModalBottomSheet.*?^\s*\}\s*^\s*\}', re.DOTALL | re.MULTILINE)
# Since the regex might be tricky, let's just replace from 'if (showBookmarksSheet) {' to the matching bracket.
