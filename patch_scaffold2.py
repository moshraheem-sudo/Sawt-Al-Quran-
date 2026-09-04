import re

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'r') as f:
    content = f.read()

pattern = r"            Scaffold\(\s*topBar = \{\s*Box\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.height\(56\.dp\)\s*\.background\(MaterialTheme\.colorScheme\.surface\)\s*\.padding\(horizontal = 8\.dp\),\s*contentAlignment = Alignment\.Center\s*\) \{\s*Row\(\s*verticalAlignment = Alignment\.CenterVertically,\s*horizontalArrangement = Arrangement\.Center\s*\) \{\s*Icon\(\s*imageVector = Icons\.Default\.Settings,\s*contentDescription = null,\s*tint = MaterialTheme\.colorScheme\.primary,\s*modifier = Modifier\.size\(24\.dp\)\s*\)\s*Spacer\(modifier = Modifier\.width\(10\.dp\)\)\s*Text\(\s*text = \"إعدادات التطبيق\",\s*fontWeight = FontWeight\.Bold,\s*fontSize = 20\.sp,\s*color = MaterialTheme\.colorScheme\.primary\s*\)\s*\}\s*val isRtl = androidx\.compose\.ui\.platform\.LocalLayoutDirection\.current == androidx\.compose\.ui\.unit\.LayoutDirection\.Rtl\s*IconButton\(\s*onClick = onDismiss,\s*modifier = Modifier\.align\(if \(isRtl\) Alignment\.CenterEnd else Alignment\.CenterStart\)\s*\) \{\s*Icon\(\s*imageVector = Icons\.Default\.Close,\s*contentDescription = \"إغلاق\",\s*tint = MaterialTheme\.colorScheme\.onSurface\s*\)\s*\}\s*\}\s*\},\s*containerColor = MaterialTheme\.colorScheme\.background\s*\) \{ paddingValues ->\s*Column\(\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.padding\(paddingValues\)\s*\.padding\(horizontal = 20\.dp, vertical = 16\.dp\)\s*\.verticalScroll\(rememberScrollState\(\)\),\s*verticalArrangement = Arrangement\.spacedBy\(16\.dp\),\s*horizontalAlignment = Alignment\.CenterHorizontally\s*\) \{"

new_scaffold = """            Scaffold(
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "إعدادات التطبيق",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        val isRtl = androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(if (isRtl) Alignment.CenterEnd else Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }"""

if re.search(pattern, content):
    content = re.sub(pattern, new_scaffold, content)
    with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Regex not matched")
