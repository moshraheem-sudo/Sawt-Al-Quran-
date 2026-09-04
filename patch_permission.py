with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
"""
if "import android.Manifest" not in content:
    content = content.replace("import android.os.Bundle", imports + "import android.os.Bundle")

permission_code = """                    val context = LocalContext.current
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            // Optional: Handle permission granted/denied
                        }
                        LaunchedEffect(Unit) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                    
                    val navController = rememberNavController()"""

content = content.replace("                    val context = LocalContext.current\n                    val navController = rememberNavController()", permission_code)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
