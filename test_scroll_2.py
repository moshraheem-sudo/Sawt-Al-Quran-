with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_launched_effect = """    LaunchedEffect(playingAyah, autoScrollEnabled, targetPage, textLayoutResults[targetPage ?: -1]) {
        if (autoScrollEnabled && playingAyah != null && targetPage != null) {
            val (playingSurahId, playingAyahNumber) = playingAyah
            if (playingSurahId == surahId) {
                val targetPageIndex = pageGroups.keys.toList().indexOf(targetPage)
                if (targetPageIndex != -1) {
                    val targetPageAyahs = pageGroups[targetPage] ?: emptyList()
                    val targetAnnotatedText = buildPageAnnotatedString(
                        pageAyahs = targetPageAyahs,
                        playingAyah = playingAyah,
                        bookmarkedAyahNumbers = bookmarkedAyahNumbers,
                        highlightedAyahNumber = highlightedAyahNumber,
                        goldAccentColor = goldAccentColor,
                        textScale = textScale,
                        surahId = surahId
                    )
                    val annotations = targetAnnotatedText.getStringAnnotations(tag = "AYAH", start = 0, end = targetAnnotatedText.length)
                    val matchingAnnotation = annotations.find { it.item == playingAyahNumber.toString() }
                    
                    val scrollOffset = if (matchingAnnotation != null) {
                        val layoutResult = textLayoutResults[targetPage]
                        if (layoutResult != null) {
                            val startCharIndex = matchingAnnotation.start
                            val line = layoutResult.getLineForOffset(startCharIndex)
                            val lineTop = layoutResult.getLineTop(line)
                            (lineTop.toInt() - 100).coerceAtLeast(0)
                        } else {
                            0
                        }
                    } else {
                        0
                    }

                    listState.animateScrollToItem(targetPageIndex, scrollOffset)
                }
            }
        }
    }"""

new_launched_effect = """    LaunchedEffect(playingAyah, autoScrollEnabled, targetPage, textLayoutResults[targetPage ?: -1]) {
        if (autoScrollEnabled && playingAyah != null && targetPage != null) {
            val (playingSurahId, playingAyahNumber) = playingAyah
            if (playingSurahId == surahId) {
                val targetPageIndex = pageGroups.keys.toList().indexOf(targetPage)
                if (targetPageIndex != -1) {
                    val targetPageAyahs = pageGroups[targetPage] ?: emptyList()
                    val targetAnnotatedText = buildPageAnnotatedString(
                        pageAyahs = targetPageAyahs,
                        playingAyah = playingAyah,
                        bookmarkedAyahNumbers = bookmarkedAyahNumbers,
                        highlightedAyahNumber = highlightedAyahNumber,
                        goldAccentColor = goldAccentColor,
                        textScale = textScale,
                        surahId = surahId
                    )
                    val annotations = targetAnnotatedText.getStringAnnotations(tag = "AYAH", start = 0, end = targetAnnotatedText.length)
                    val matchingAnnotation = annotations.find { it.item == playingAyahNumber.toString() }
                    
                    val scrollOffset = if (matchingAnnotation != null) {
                        val layoutResult = textLayoutResults[targetPage]
                        if (layoutResult != null) {
                            val startCharIndex = matchingAnnotation.start
                            val line = layoutResult.getLineForOffset(startCharIndex)
                            val lineTop = layoutResult.getLineTop(line)
                            (lineTop.toInt() - 100).coerceAtLeast(0)
                        } else {
                            0
                        }
                    } else {
                        0
                    }

                    listState.animateScrollToItem(targetPageIndex, scrollOffset)
                }
            }
        }
    }

    // Scroll to highlighted ayah exactly
    var hasScrolledToHighlight by remember(highlightedAyahNumber) { mutableStateOf(false) }
    val highlightedTargetPage = highlightedAyahNumber?.let { pageMap[it] ?: startPage }
    LaunchedEffect(highlightedAyahNumber, highlightedTargetPage, textLayoutResults[highlightedTargetPage ?: -1]) {
        if (highlightedAyahNumber != null && highlightedTargetPage != null && !hasScrolledToHighlight) {
            val targetPageIndex = pageGroups.keys.toList().indexOf(highlightedTargetPage)
            if (targetPageIndex != -1) {
                val targetPageAyahs = pageGroups[highlightedTargetPage] ?: emptyList()
                val targetAnnotatedText = buildPageAnnotatedString(
                    pageAyahs = targetPageAyahs,
                    playingAyah = playingAyah,
                    bookmarkedAyahNumbers = bookmarkedAyahNumbers,
                    highlightedAyahNumber = highlightedAyahNumber,
                    goldAccentColor = goldAccentColor,
                    textScale = textScale,
                    surahId = surahId
                )
                val annotations = targetAnnotatedText.getStringAnnotations(tag = "AYAH", start = 0, end = targetAnnotatedText.length)
                val matchingAnnotation = annotations.find { it.item == highlightedAyahNumber.toString() }
                
                val layoutResult = textLayoutResults[highlightedTargetPage]
                if (layoutResult != null) {
                    val scrollOffset = if (matchingAnnotation != null) {
                        val startCharIndex = matchingAnnotation.start
                        val line = layoutResult.getLineForOffset(startCharIndex)
                        val lineTop = layoutResult.getLineTop(line)
                        (lineTop.toInt() - 200).coerceAtLeast(0) // scroll to make it comfortably visible
                    } else {
                        0
                    }
                    delay(100) // Ensure layout bounds are fresh
                    listState.scrollToItem(targetPageIndex, scrollOffset)
                    hasScrolledToHighlight = true
                } else {
                    // Force the page to render first so we can get its layout result
                    listState.scrollToItem(targetPageIndex)
                }
            }
        }
    }"""

content = content.replace(old_launched_effect, new_launched_effect)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)

