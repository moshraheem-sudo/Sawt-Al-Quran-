import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.material.icons.filled.KeyboardArrowDown', 'import androidx.compose.material.icons.filled.KeyboardArrowDown\nimport androidx.compose.material.icons.filled.KeyboardArrowRight\nimport androidx.compose.material.icons.filled.KeyboardArrowLeft')

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
