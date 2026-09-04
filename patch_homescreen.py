import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    text = f.read()

# 1. Insert `var isAyahCardExpanded by remember { mutableStateOf(true) }`
text = text.replace(
    "var lastRead by remember { mutableStateOf<LastReadItem?>(null) }",
    "var lastRead by remember { mutableStateOf<LastReadItem?>(null) }\n    var isAyahCardExpanded by remember { mutableStateOf(true) }"
)

# 2. Modify the card
card_pattern = r"""(item \{\s*val randomAyah by viewModel\.randomAyah\.collectAsStateWithLifecycle\(\)\s*val randomSurahName by viewModel\.randomAyahSurahName\.collectAsStateWithLifecycle\(\)\s*if \(randomAyah != null\) \{)(.*?)(Spacer\(modifier = Modifier\.height\(12\.dp\)\)\s*\}\s*\})"""

# Let's write a targeted replacement for the card block
