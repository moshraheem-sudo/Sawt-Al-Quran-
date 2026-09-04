with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import re

imports = """import kotlinx.coroutines.flow.MutableStateFlow
import android.content.Intent
"""
if "import kotlinx.coroutines.flow.MutableStateFlow" not in content:
    content = content.replace("import android.os.Bundle", imports + "import android.os.Bundle")

flow_code = """    private val pendingNavigation = MutableStateFlow<String?>(null)

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        checkIntentForNavigation(intent)
    }

    private fun checkIntentForNavigation(intent: Intent?) {
        val surahId = intent?.getIntExtra("open_surah_id", -1) ?: -1
        val ayahNumber = intent?.getIntExtra("open_ayah_number", -1) ?: -1
        if (surahId != -1) {
            val route = if (ayahNumber != -1) "reader/$surahId?ayah=$ayahNumber" else "reader/$surahId"
            pendingNavigation.value = route
            
            // clear extras to avoid handling them again
            intent?.removeExtra("open_surah_id")
            intent?.removeExtra("open_ayah_number")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {"""

if "private val pendingNavigation" not in content:
    content = content.replace("    override fun onCreate(savedInstanceState: Bundle?) {", flow_code)

nav_code = """                    val navController = rememberNavController()
                    
                    // Handle initial intent
                    LaunchedEffect(Unit) {
                        checkIntentForNavigation(intent)
                    }

                    // Observe pending navigation
                    val pendingRoute by pendingNavigation.collectAsState(initial = null)
                    LaunchedEffect(pendingRoute) {
                        if (pendingRoute != null) {
                            navController.navigate(pendingRoute!!)
                            pendingNavigation.value = null
                        }
                    }"""

content = content.replace("                    val navController = rememberNavController()", nav_code)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
