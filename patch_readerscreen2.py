import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if line.strip() == 'var showBookmarksSheet by remember { mutableStateOf(false) }':
        continue
    
    if line.strip() == 'if (showBookmarksSheet) {':
        skip = True
    
    if skip:
        if line.strip() == 'if (showReciterDialog) {':
            skip = False
        else:
            continue
            
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.writelines(new_lines)
