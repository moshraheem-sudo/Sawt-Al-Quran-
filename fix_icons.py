with open("app/src/main/java/com/example/ui/screens/KhatmaScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Default.Refresh", "androidx.compose.material.icons.Icons.Default.Refresh") # wait this is what it is now

content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Refresh")

content = content.replace("androidx.compose.material.icons.Icons.Default.Refresh", "Icons.Default.Refresh")

with open("app/src/main/java/com/example/ui/screens/KhatmaScreen.kt", "w") as f:
    f.write(content)
