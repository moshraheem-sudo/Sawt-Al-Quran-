with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith('import androidx.lifecycle.ViewModelProvider') and len(new_lines) == 0:
        continue # skip it
    new_lines.append(line)

# insert package first if not there
if not new_lines[0].startswith('package'):
    for i, line in enumerate(new_lines):
        if line.startswith('package'):
            pkg = new_lines.pop(i)
            new_lines.insert(0, pkg)
            break

# insert ViewModelProvider
new_lines.insert(1, 'import androidx.lifecycle.ViewModelProvider\n')

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'w') as f:
    f.writelines(new_lines)
