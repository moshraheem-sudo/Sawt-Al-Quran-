with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith("import androidx.compose.runtime.collectAsState"):
        continue
    new_lines.append(line)

# find where imports start
for i, line in enumerate(new_lines):
    if line.startswith("import "):
        new_lines.insert(i, "import androidx.compose.runtime.collectAsState\n")
        break

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(new_lines)
