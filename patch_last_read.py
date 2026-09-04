with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import com.example.data.local.LastReadManager
"""
if "import com.example.data.local.LastReadManager" not in content:
    content = content.replace("import com.example.ui.viewmodels.ReaderViewModel", imports + "import com.example.ui.viewmodels.ReaderViewModel")

code = """
    val pageMap = remember(surahId, ayahs) { buildPageMap(surahId, ayahs) }

    val pageGroups = remember(ayahs, pageMap, startPage) {
        val groups = mutableMapOf<Int, MutableList<AyahEntity>>()
        ayahs.forEach { ayah ->
            val p = pageMap[ayah.ayahNumber] ?: startPage
            groups.getOrPut(p) { mutableListOf() }.add(ayah)
        }
        groups.toSortedMap()
    }
    
    val context = LocalContext.current
    LaunchedEffect(listState, pageGroups, surahId) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (pageGroups.isNotEmpty() && index < pageGroups.size) {
                    val pageEntries = pageGroups.entries.toList()
                    val visiblePage = pageEntries[index]
                    val firstAyah = visiblePage.value.firstOrNull()
                    if (firstAyah != null) {
                        LastReadManager.saveLastRead(context, surahId, surahName, firstAyah.ayahNumber)
                    }
                }
            }
    }
"""

if "LastReadManager.saveLastRead" not in content:
    import re
    # We replace the pageGroups block
    pattern = r"val pageGroups = remember\(ayahs, pageMap, startPage\) \{.*?\n    \}"
    content = re.sub(pattern, code.strip(), content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
