import re
with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    text = f.read()

text = text.replace("import androidx.compose.material.icons.filled.KeyboardArrowUpBorder", "import androidx.compose.material.icons.filled.BookmarkBorder")

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(text)
