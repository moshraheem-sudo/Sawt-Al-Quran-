import re

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'r') as f:
    content = f.read()

# 1. Add imports if needed
imports = """import com.example.data.local.NotificationSettingsManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.AyahWorker
"""
if "NotificationSettingsManager" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\n" + imports)

# 2. Update existing states to be false by default
content = content.replace(
    "var isQuranUpdateExpanded by remember { mutableStateOf(true) }",
    "var isQuranUpdateExpanded by remember { mutableStateOf(false) }"
)

# 3. Add new states
new_states = """    var isNourUpdateExpanded by remember { mutableStateOf(false) }
    var isDeveloperCardExpanded by remember { mutableStateOf(false) }
    var isAppInfoExpanded by remember { mutableStateOf(false) }
    var isNotificationsExpanded by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(NotificationSettingsManager.areNotificationsEnabled(context)) }
"""
content = content.replace("    var isNourUpdateExpanded by remember { mutableStateOf(false) }\n", new_states)

# 4. Collapse Card 4 (Developer Card)
card4_old = """                    // CARD 4: Developer Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {"""
card4_new = """                    // CARD 4: Developer Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDeveloperCardExpanded = !isDeveloperCardExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "المطور",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isDeveloperCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isDeveloperCardExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            AnimatedVisibility(visible = isDeveloperCardExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {"""
content = content.replace(card4_old, card4_new)

# Close Card 4 AnimatedVisibility
card4_end_old = """                                    )
                                }
                            }
                        }
                    }

                    // App Info"""
card4_end_new = """                                    )
                                }
                            }
                            }
                        }
                    }

                    // App Info"""
content = content.replace(card4_end_old, card4_end_new)


# 5. Collapse App Info Card
card5_old = """                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {"""
card5_new = """                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAppInfoExpanded = !isAppInfoExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "عن التطبيق",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isAppInfoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isAppInfoExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedVisibility(visible = isAppInfoExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {"""
content = content.replace(card5_old, card5_new)
content = content.replace("""                        }
                    }
                }
            }
        }
    }
}""", """                        }
                            }
                        }
                    }
                }
            }
        }
    }
}""")

# 6. Insert Notifications Card before Card 1
notification_card = """                    // NOTIFICATIONS CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isNotificationsExpanded = !isNotificationsExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "إعدادات الإشعارات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isNotificationsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isNotificationsExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            AnimatedVisibility(visible = isNotificationsExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "إشعارات آية اليوم",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "إرسال 10 إشعارات بالآيات القرآنية القصيرة خلال الساعة (إشعار كل 6 دقائق)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = notificationsEnabled,
                                            onCheckedChange = { enabled ->
                                                notificationsEnabled = enabled
                                                NotificationSettingsManager.setNotificationsEnabled(context, enabled)
                                                if (enabled) {
                                                    val nextWork = OneTimeWorkRequestBuilder<AyahWorker>()
                                                        .setInitialDelay(6, TimeUnit.MINUTES)
                                                        .build()
                                                    WorkManager.getInstance(context).enqueueUniqueWork(
                                                        "AyahNotificationWork",
                                                        androidx.work.ExistingWorkPolicy.REPLACE,
                                                        nextWork
                                                    )
                                                    Toast.makeText(context, "تم تفعيل الإشعارات (إشعار كل 6 دقائق)", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    WorkManager.getInstance(context).cancelUniqueWork("AyahNotificationWork")
                                                    Toast.makeText(context, "تم إيقاف الإشعارات", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    """

content = content.replace("                    // CARD 1: App Theme Selection Card", notification_card + "                    // CARD 1: App Theme Selection Card")

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'w') as f:
    f.write(content)
