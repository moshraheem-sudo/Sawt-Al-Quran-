with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_scroll = """    // Scroll to initial Ayah if provided
    var hasScrolledToInitial by remember { mutableStateOf(false) }
    LaunchedEffect(ayahs, isMushafMode) {
        if (ayahs.isNotEmpty() && initialAyah != -1 && !hasScrolledToInitial) {
            kotlinx.coroutines.delay(300) // Wait for layout to finish
            if (isMushafMode) {
                val startPage = SURAH_START_PAGES.getOrElse(activeSurahId - 1) { 1 }
                val pageForAyah = pageMap[initialAyah] ?: startPage
                val allPages = ayahs.map { pageMap[it.ayahNumber] ?: startPage }.distinct().sorted()
                val pageIndex = allPages.indexOf(pageForAyah)
                if (pageIndex >= 0) {
                    listState.animateScrollToItem(pageIndex)
                    highlightedAyahNumber = initialAyah
                }
            } else {
                val index = ayahs.indexOfFirst { it.ayahNumber == initialAyah }
                if (index >= 0) {
                    // Add 1 for the Bismillah header if present
                    val scrollIndex = if (activeSurahId != 1 && activeSurahId != 9) index + 1 else index
                    // Scroll so the ayah is somewhat centered or clearly visible
                    listState.animateScrollToItem(scrollIndex)
                    highlightedAyahNumber = initialAyah
                }
            }
            hasScrolledToInitial = true
        }
    }"""

new_scroll = """    // Scroll to initial Ayah if provided
    var hasScrolledToInitial by remember { mutableStateOf(false) }
    LaunchedEffect(ayahs, isMushafMode) {
        if (ayahs.isNotEmpty() && initialAyah != -1 && !hasScrolledToInitial) {
            kotlinx.coroutines.delay(400) // Give UI time to draw items
            highlightedAyahNumber = initialAyah
            hasScrolledToInitial = true
        }
    }"""

content = content.replace(old_scroll, new_scroll)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
