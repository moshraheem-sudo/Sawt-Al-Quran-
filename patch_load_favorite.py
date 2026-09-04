with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'r') as f:
    content = f.read()

old_fallback_1 = """                } else {
                    reciterName = recitersMap[rId] ?: "قارئ #$rId"
                    styleName = if (styleId == 1) "المصحف المجود" else "المصحف المرتل"
                    server = ""
                    surahName = SURAH_NAMES.getOrNull(surahId - 1) ?: "سورة #$surahId"
                }
            } else {
                reciterName = recitersMap[rId] ?: "قارئ #$rId"
                styleName = if (styleId == 1) "المصحف المجود" else "المصحف المرتل"
                server = ""
                surahName = SURAH_NAMES.getOrNull(surahId - 1) ?: "سورة #$surahId"
            }"""

new_fallback_1 = """                } else {
                    reciterName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                    val defaultStyle = if (styleId == 1) "المصحف المجود" else if (styleId == 2) "المصحف المرتل" else "قراءة $styleId"
                    styleName = prefs.getString("style_name_$styleId", defaultStyle) ?: defaultStyle
                    server = ""
                    surahName = SURAH_NAMES.getOrNull(surahId - 1) ?: "سورة #$surahId"
                }
            } else {
                reciterName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                val defaultStyle = if (styleId == 1) "المصحف المجود" else if (styleId == 2) "المصحف المرتل" else "قراءة $styleId"
                styleName = prefs.getString("style_name_$styleId", defaultStyle) ?: defaultStyle
                server = ""
                surahName = SURAH_NAMES.getOrNull(surahId - 1) ?: "سورة #$surahId"
            }"""

content = content.replace(old_fallback_1, new_fallback_1)

with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'w') as f:
    f.write(content)
