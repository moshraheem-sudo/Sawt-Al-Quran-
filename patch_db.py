with open('app/src/main/java/com/example/data/local/LocalDatabase.kt', 'r') as f:
    content = f.read()

new_dao = """    @Query("SELECT nameAr FROM surahs WHERE id = :id LIMIT 1")
    suspend fun getSurahNameById(id: Int): String?

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId AND ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getAyahByNumber(surahId: Int, ayahNumber: Int): AyahEntity?
"""

content = content.replace('    @Query("SELECT nameAr FROM surahs WHERE id = :id LIMIT 1")\n    suspend fun getSurahNameById(id: Int): String?', new_dao)

with open('app/src/main/java/com/example/data/local/LocalDatabase.kt', 'w') as f:
    f.write(content)
