import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Icons.AutoMirrored.Filled.KeyboardArrowRight', 'Icons.Default.KeyboardArrowRight')
content = content.replace('Icons.AutoMirrored.Filled.KeyboardArrowLeft', 'Icons.Default.KeyboardArrowLeft')

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
