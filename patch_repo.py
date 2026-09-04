with open('app/src/main/java/com/example/data/repository/QuranRepository.kt', 'r') as f:
    content = f.read()

new_method = """    suspend fun getSurahNameById(id: Int): String? {
        return dao.getSurahNameById(id)
    }

    suspend fun getAyahByNumber(surahId: Int, ayahNumber: Int): AyahEntity? {
        return dao.getAyahByNumber(surahId, ayahNumber)
    }
"""

content = content.replace("""    suspend fun getSurahNameById(id: Int): String? {
        return dao.getSurahNameById(id)
    }""", new_method)

with open('app/src/main/java/com/example/data/repository/QuranRepository.kt', 'w') as f:
    f.write(content)
