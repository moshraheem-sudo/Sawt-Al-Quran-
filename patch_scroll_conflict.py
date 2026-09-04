with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

# Replace the block that handles initialAyah to be more robust:
old_initial_scroll = """    // Scroll to initialAyah if provided from search
    LaunchedEffect(ayahs, initialAyah) {
        if (initialAyah > 0 && ayahs.isNotEmpty()) {
            delay(150)
            val index = ayahs.indexOfFirst { it.ayahNumber == initialAyah }
            if (index != -1) {
                if (isMushafMode) {
                    val startPage = SURAH_START_PAGES.getOrElse(activeSurahId - 1) { 1 }
                    val targetPage = pageMap[initialAyah] ?: startPage
                    val pageOffset = (targetPage - startPage).coerceAtLeast(0)
                    listState.scrollToItem(pageOffset)
                } else {
                    listState.scrollToItem((index + 1).coerceAtMost(ayahs.size))
                }
            }
        }
    }"""

new_initial_scroll = """    // Scroll to initialAyah if provided from search
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

content = content.replace(old_initial_scroll, new_initial_scroll)

# Replace the surahChanged blocks to avoid resetting scroll if initialAyah > 0 and we haven't scrolled to it yet.
# Actually, the easiest way is to just ignore scrollToItem(0) if initialAyah > 0.
old_playing_scroll = """            } ?: run {
                if (surahChanged) {
                    listState.scrollToItem(0)
                }
            }
        } else if (ayahs.isNotEmpty() && !isMushafMode) {
            val surahChanged = lastSurahId != activeSurahId
            lastSurahId = activeSurahId
            if (surahChanged) {
                listState.scrollToItem(0)
            }
        }"""

new_playing_scroll = """            } ?: run {
                if (surahChanged && initialAyah <= 0) {
                    listState.scrollToItem(0)
                }
            }
        } else if (ayahs.isNotEmpty() && !isMushafMode) {
            val surahChanged = lastSurahId != activeSurahId
            lastSurahId = activeSurahId
            if (surahChanged && initialAyah <= 0) {
                listState.scrollToItem(0)
            }
        }"""

content = content.replace(old_playing_scroll, new_playing_scroll)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
