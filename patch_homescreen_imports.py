with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
imports = set()
for line in lines:
    if line.startswith('import '):
        if line in imports:
            continue
        imports.add(line)
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.writelines(new_lines)
