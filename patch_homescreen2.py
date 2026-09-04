import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    text = f.read()

# 1. Insert `var isAyahCardExpanded by remember { mutableStateOf(true) }`
text = text.replace(
    "var lastRead by remember { mutableStateOf<LastReadItem?>(null) }",
    "var lastRead by remember { mutableStateOf<LastReadItem?>(null) }\n    var isAyahCardExpanded by remember { mutableStateOf(true) }"
)

# 2. Add imports if needed
if "import androidx.compose.material.icons.filled.KeyboardArrowDown" not in text:
    text = text.replace(
        "import androidx.compose.material.icons.filled.Bookmark",
        "import androidx.compose.material.icons.filled.Bookmark\nimport androidx.compose.material.icons.filled.KeyboardArrowDown\nimport androidx.compose.material.icons.filled.KeyboardArrowUp"
    )

# 3. Replace card
old_card = r"""item \{\s*val randomAyah by viewModel\.randomAyah\.collectAsStateWithLifecycle\(\)[\s\S]*?Spacer\(modifier = Modifier\.height\(12\.dp\)\)\s*\}\s*\}"""

new_card = """item {
                    val randomAyah by viewModel.randomAyah.collectAsStateWithLifecycle()
                    val randomSurahName by viewModel.randomAyahSurahName.collectAsStateWithLifecycle()
                    
                    if (randomAyah != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isAyahCardExpanded = !isAyahCardExpanded }
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "قبس من كتاب الله تعالى",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isAyahCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isAyahCardExpanded) "طي" else "توسيع",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                androidx.compose.animation.AnimatedVisibility(visible = isAyahCardExpanded) {
                                    androidx.compose.animation.AnimatedContent(
                                        targetState = randomAyah,
                                        transitionSpec = {
                                            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)).togetherWith(androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)))
                                        },
                                        label = "ayah_animation"
                                    ) { targetAyah ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onAyahSelected(randomAyah!!.surahId, randomAyah!!.ayahNumber)
                                                }
                                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = targetAyah!!.textUthmani,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Normal,
                                                fontFamily = FontFamily.Serif,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(bottom = 12.dp),
                                                lineHeight = 36.sp
                                            )
                                            
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "سورة $randomSurahName • الآية ${targetAyah.ayahNumber}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }"""

text = re.sub(old_card, new_card, text, count=1)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(text)

