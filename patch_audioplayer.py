with open('app/src/main/java/com/example/audio/AudioPlayerManager.kt', 'r') as f:
    content = f.read()

imports = """import com.example.data.local.LastReadManager
import com.example.ui.screens.SURAH_NAMES_AR
"""
if "import com.example.data.local.LastReadManager" not in content:
    content = content.replace("import com.example.R", imports + "import com.example.R")

# Find where _currentPlayingAyah is updated
# It's updated in playTrack logic or similar.
# Wait, let's search for _currentPlayingAyah.value =
