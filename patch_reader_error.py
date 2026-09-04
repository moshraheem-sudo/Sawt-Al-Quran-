import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_logic = """                                                        if (isMushafMode) {
                                                            val pageIndex = pageGroups.keys.sorted().indexOfFirst { page ->
                                                                pageGroups[page]?.any { it.ayahNumber == bookmark.ayahNumber } == true
                                                            }
                                                            if (pageIndex >= 0) {
                                                                listState.animateScrollToItem(pageIndex)
                                                            }
                                                        } else {"""
new_logic = """                                                        if (isMushafMode) {
                                                            val startPage = SURAH_START_PAGES.getOrElse(activeSurahId - 1) { 1 }
                                                            val pageForAyah = pageMap[bookmark.ayahNumber] ?: startPage
                                                            val allPages = ayahs.map { pageMap[it.ayahNumber] ?: startPage }.distinct().sorted()
                                                            val pageIndex = allPages.indexOf(pageForAyah)
                                                            if (pageIndex >= 0) {
                                                                listState.animateScrollToItem(pageIndex)
                                                            }
                                                        } else {"""
content = content.replace(old_logic, new_logic)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
