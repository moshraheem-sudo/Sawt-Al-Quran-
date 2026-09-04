import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

if 'Icons.AutoMirrored.Filled.KeyboardArrowRight' not in content:
    print('Not found')
