with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
imports = set()
for line in lines:
    if line.startswith('import '):
        if line in imports or 'BookmarkManagerProvider' in line:
            continue
        imports.add(line)
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'w') as f:
    f.writelines(new_lines)
