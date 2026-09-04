with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'r') as f:
    content = f.read()

content = content.replace('var quranUpcomingVersion by remember { mutableStateOf("1.30.0") }', 'var quranUpcomingVersion by remember { mutableStateOf(quranCurrentVersion) }')
content = content.replace('text = "الإصدار: v1.30.0"', 'text = "الإصدار: $quranCurrentVersion"')

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'w') as f:
    f.write(content)
