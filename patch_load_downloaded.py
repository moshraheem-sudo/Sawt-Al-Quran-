with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'r') as f:
    content = f.read()

old_func = """fun loadDownloadedItems(context: Context): List<DownloadedItem> {
    val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
    if (!audioDir.exists()) return emptyList()
    val files = audioDir.listFiles() ?: return emptyList()
    val items = mutableListOf<DownloadedItem>()
    val recitersMap = mapOf(
        51 to "عبد الباسط عبد الصمد",
        112 to "محمد صديق المنشاوي",
        118 to "محمود خليل الحصري",
        125 to "مصطفى إسماعيل",
        81 to "فارس عباد",
        30 to "سعد الغامدي",
        241 to "محمد رفعت"
    )
    for (file in files) {
        if (file.isFile && file.name.endsWith(".mp3")) {
            val nameWithoutExt = file.nameWithoutExtension
            val parts = nameWithoutExt.split("_")
            if (parts.size == 3) {
                val rId = parts[0].toIntOrNull() ?: continue
                val sStyleId = parts[1].toIntOrNull() ?: 1
                val sId = parts[2].toIntOrNull() ?: continue
                val rName = recitersMap[rId] ?: "قارئ #$rId"
                val styleName = if (sStyleId == 1) "المصحف المجود" else "المصحف المرتل"
                val surahName = SURAH_NAMES.getOrNull(sId - 1) ?: "سورة #$sId"
                items.add(
                    DownloadedItem(
                        fileKey = nameWithoutExt,
                        reciterId = rId,
                        reciterName = rName,
                        styleId = sStyleId,
                        styleName = styleName,
                        surahId = sId,
                        surahName = surahName,
                        file = file
                    )
                )
            }
        }
    }
    return items.sortedWith(compareBy({ it.reciterName }, { it.styleName }, { it.surahId }))
}"""

new_func = """fun loadDownloadedItems(context: Context): List<DownloadedItem> {
    val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
    if (!audioDir.exists()) return emptyList()
    val files = audioDir.listFiles() ?: return emptyList()
    val items = mutableListOf<DownloadedItem>()
    val prefs = context.getSharedPreferences("reciter_prefs", Context.MODE_PRIVATE)
    
    val recitersMap = mapOf(
        51 to "عبد الباسط عبد الصمد",
        112 to "محمد صديق المنشاوي",
        118 to "محمود خليل الحصري",
        125 to "مصطفى إسماعيل",
        81 to "فارس عباد",
        30 to "سعد الغامدي",
        241 to "محمد رفعت"
    )
    for (file in files) {
        if (file.isFile && file.name.endsWith(".mp3")) {
            val nameWithoutExt = file.nameWithoutExtension
            val parts = nameWithoutExt.split("_")
            if (parts.size == 3) {
                val rId = parts[0].toIntOrNull() ?: continue
                val sStyleId = parts[1].toIntOrNull() ?: 1
                val sId = parts[2].toIntOrNull() ?: continue
                
                // Fallback map for style names if not found in SharedPreferences
                val defaultStyleName = if (sStyleId == 1) "المصحف المجود" else if (sStyleId == 2) "المصحف المرتل" else "قراءة $sStyleId"
                
                val rName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                val styleName = prefs.getString("style_name_$sStyleId", defaultStyleName) ?: defaultStyleName
                
                val surahName = SURAH_NAMES.getOrNull(sId - 1) ?: "سورة #$sId"
                items.add(
                    DownloadedItem(
                        fileKey = nameWithoutExt,
                        reciterId = rId,
                        reciterName = rName,
                        styleId = sStyleId,
                        styleName = styleName,
                        surahId = sId,
                        surahName = surahName,
                        file = file
                    )
                )
            }
        }
    }
    return items.sortedWith(compareBy({ it.reciterName }, { it.styleName }, { it.surahId }))
}"""

content = content.replace(old_func, new_func)

with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'w') as f:
    f.write(content)
