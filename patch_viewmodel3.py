import re

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('if (!kotlinx.coroutines.isActive) break', 'if (!isActive) break')

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'w') as f:
    f.write(content)
