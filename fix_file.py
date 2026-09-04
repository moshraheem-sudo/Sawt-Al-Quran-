with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith('import androidx.compose.material3.AlertDialog'): continue
    if line.startswith('import androidx.compose.material3.TextButton'): continue
    new_lines.append(line)

new_lines.insert(2, "import androidx.compose.material3.AlertDialog\n")
new_lines.insert(3, "import androidx.compose.material3.TextButton\n")

with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'w') as f:
    f.writelines(new_lines)
