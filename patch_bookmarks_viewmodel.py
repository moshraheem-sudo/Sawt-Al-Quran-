import re

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'r') as f:
    content = f.read()

# Replace QuranBookmark definition
pattern_remove = re.compile(r'data class QuranBookmark\(.*?\)\s*', re.DOTALL)
content = pattern_remove.sub('', content)

# Change the imports if needed (import com.example.data.local.QuranBookmark and BookmarkManager)
if "import com.example.data.local.QuranBookmark" not in content:
    content = content.replace('import androidx.lifecycle.ViewModel', 'import androidx.lifecycle.ViewModel\nimport com.example.data.local.QuranBookmark\nimport com.example.data.local.BookmarkManager')

# Replace initBookmarks and loadBookmarks
old_load = """    fun initBookmarks(context: Context) {
        if (isPrefsLoaded) return
        isPrefsLoaded = true
        loadBookmarks(context)
    }

    private fun loadBookmarks(context: Context) {
        val sharedPrefs = context.getSharedPreferences("quran_bookmarks_pref", Context.MODE_PRIVATE)
        val jsonStr = sharedPrefs.getString("bookmarks_json", "[]") ?: "[]"
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<QuranBookmark>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QuranBookmark(
                        id = obj.getString("id"),
                        surahId = obj.getInt("surahId"),
                        surahName = obj.getString("surahName"),
                        ayahNumber = obj.getInt("ayahNumber"),
                        ayahText = obj.getString("ayahText"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            _bookmarks.value = list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            _bookmarks.value = emptyList()
        }
    }"""
new_load = """    fun initBookmarks(context: Context) {
        if (isPrefsLoaded) return
        isPrefsLoaded = true
        _bookmarks.value = BookmarkManager.loadBookmarks(context)
    }"""
content = content.replace(old_load, new_load)

# Replace saveBookmarks
old_save = """    private fun saveBookmarks(context: Context) {
        val sharedPrefs = context.getSharedPreferences("quran_bookmarks_pref", Context.MODE_PRIVATE)
        val array = JSONArray()
        _bookmarks.value.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("surahId", b.surahId)
                put("surahName", b.surahName)
                put("ayahNumber", b.ayahNumber)
                put("ayahText", b.ayahText)
                put("timestamp", b.timestamp)
            }
            array.put(obj)
        }
        sharedPrefs.edit().putString("bookmarks_json", array.toString()).apply()
    }"""
new_save = """    private fun saveBookmarks(context: Context) {
        BookmarkManager.saveBookmarks(context, _bookmarks.value)
    }"""
content = content.replace(old_save, new_save)

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'w') as f:
    f.write(content)
