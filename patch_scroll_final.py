with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

# Make the delay longer and ensure scrolling works reliably
old_initial_scroll = """    // Scroll to initialAyah if provided from search
    var hasScrolledToInitial by remember(initialAyah) { mutableStateOf(false) }
    LaunchedEffect(ayahs, initialAyah, isMushafMode) {
        if (initialAyah > 0 && ayahs.isNotEmpty() && !hasScrolledToInitial) {
            delay(300) // allow layout to complete
            val index = ayahs.indexOfFirst { it.ayahNumber == initialAyah }
            if (index != -1) {
                if (isMushafMode) {
                    val startPage = SURAH_START_PAGES.getOrElse(activeSurahId - 1) { 1 }
                    val targetPage = pageMap[initialAyah] ?: startPage
                    val pageOffset = (targetPage - startPage).coerceAtLeast(0)
                    listState.animateScrollToItem(pageOffset)
                } else {
                    val scrollIndex = (index).coerceAtMost(ayahs.size) // slightly before to show context
                    listState.animateScrollToItem(scrollIndex)
                }
            }
            hasScrolledToInitial = true
        }
    }"""

new_initial_scroll = """    // Scroll to initialAyah if provided from search
    var hasScrolledToInitial by remember(initialAyah) { mutableStateOf(false) }
    LaunchedEffect(ayahs, initialAyah, isMushafMode) {
        if (initialAyah > 0 && ayahs.isNotEmpty() && !hasScrolledToInitial) {
            delay(500) // wait for lazy column to populate items and layout
            val index = ayahs.indexOfFirst { it.ayahNumber == initialAyah }
            if (index != -1) {
                if (isMushafMode) {
                    val startPage = SURAH_START_PAGES.getOrElse(activeSurahId - 1) { 1 }
                    val targetPage = pageMap[initialAyah] ?: startPage
                    val pageOffset = (targetPage - startPage).coerceAtLeast(0)
                    listState.scrollToItem(pageOffset)
                } else {
                    val scrollIndex = (index).coerceAtMost(ayahs.size)
                    listState.scrollToItem(scrollIndex)
                }
            }
            hasScrolledToInitial = true
        }
    }"""

content = content.replace(old_initial_scroll, new_initial_scroll)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
