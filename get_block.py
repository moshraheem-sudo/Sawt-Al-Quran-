import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    text = f.read()

pattern = r"item \{\s*val randomAyah by viewModel\.randomAyah\.collectAsStateWithLifecycle\(\)[\s\S]*?Spacer\(modifier = Modifier\.height\(12\.dp\)\)\s*\}\s*\}"
match = re.search(pattern, text)
if match:
    print(match.group(0))
