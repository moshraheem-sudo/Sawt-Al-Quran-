package com.example.data.repository

import com.example.data.local.AyahEntity
import com.example.data.local.QuranDao
import com.example.data.local.SurahEntity
import com.example.data.remote.QuranApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class QuranRepository(
    private val dao: QuranDao,
    private val api: QuranApi
) {
    fun getAllSurahs(): Flow<List<SurahEntity>> = flow {
        val cached = dao.getAllSurahs().first()
        if (cached.isNotEmpty()) {
            emit(cached)
        } else {
            try {
                val response = api.getChapters()
                val entities = response.chapters.map {
                    SurahEntity(
                        id = it.id,
                        nameAr = it.name_arabic,
                        nameEn = it.name_simple,
                        revelationType = it.revelation_place,
                        ayahCount = it.verses_count
                    )
                }
                dao.insertSurahs(entities)
                emit(entities)
            } catch (e: Exception) {
                // If offline and no cache, emit empty or handle error
                emit(emptyList())
            }
        }
        // Emit from DB to keep it reactive
        dao.getAllSurahs().collect { emit(it) }
    }

    fun getSurah(surahId: Int): Flow<SurahEntity?> {
        return dao.getSurahById(surahId)
    }

    fun getAyahs(surahId: Int): Flow<List<AyahEntity>> = flow {
        val cachedCount = dao.getAyahsCountForSurah(surahId)
        if (cachedCount > 0) {
            // Already cached, just emit
            val cached = dao.getAyahsForSurah(surahId).first()
            emit(cached)
        } else {
            try {
                val response = api.getVersesByChapter(surahId)
                val entities = response.verses.map { verse ->
                    val ayahNum = verse.verse_key.split(":")[1].toInt()
                    AyahEntity(
                        surahId = surahId,
                        ayahNumber = ayahNum,
                        textUthmani = verse.text_uthmani
                    )
                }
                dao.insertAyahs(entities)
                emit(entities)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
        dao.getAyahsForSurah(surahId).collect { emit(it) }
    }

    suspend fun syncAllQuranData(onProgress: (Int) -> Unit) {
        if (dao.getSurahsCount() == 0) {
            try {
                val response = api.getChapters()
                val entities = response.chapters.map {
                    SurahEntity(
                        id = it.id,
                        nameAr = it.name_arabic,
                        nameEn = it.name_simple,
                        revelationType = it.revelation_place,
                        ayahCount = it.verses_count
                    )
                }
                dao.insertSurahs(entities)
            } catch (e: Exception) {
                // Return early if we can't even get chapters
                return
            }
        }

        // Loop over all 114 Surahs and download them to cache offline
        for (i in 1..114) {
            if (dao.getAyahsCountForSurah(i) == 0) {
                try {
                    val response = api.getVersesByChapter(i)
                    val entities = response.verses.map { verse ->
                        val ayahNum = verse.verse_key.split(":")[1].toInt()
                        AyahEntity(
                            surahId = i,
                            ayahNumber = ayahNum,
                            textUthmani = verse.text_uthmani
                        )
                    }
                    dao.insertAyahs(entities)
                } catch (e: Exception) {
                    // Silently fail and continue or retry later
                }
            }
            onProgress(i)
        }
    }

    companion object {
        private val DIACRITICS_REGEX = Regex("[\u064B-\u0652\u0670\u0653\u0654\u0655\u0610-\u061A\u06D6-\u06DC\u06DF-\u06E8\u06EA-\u06ED]")
    }

    @Volatile
    private var cachedNormalizedAyahs: List<Pair<AyahEntity, String>>? = null

    suspend fun searchAyahs(query: String): List<AyahEntity> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()

        // 1. Try raw match from DB
        val rawMatches = dao.searchAyahsRaw(trimmed)
        if (rawMatches.isNotEmpty()) {
            return rawMatches
        }

        // 2. Normalized match without diacritics using cached normalized text
        val cleanQuery = removeArabicDiacritics(trimmed)
        if (cleanQuery.isBlank()) return emptyList()

        var normalizedList = cachedNormalizedAyahs
        if (normalizedList == null || normalizedList.isEmpty()) {
            val allAyahs = dao.getAllAyahsForSearch()
            normalizedList = allAyahs.map { ayah ->
                ayah to removeArabicDiacritics(ayah.textUthmani)
            }
            if (allAyahs.isNotEmpty()) {
                cachedNormalizedAyahs = normalizedList
            }
        }

        val results = ArrayList<AyahEntity>(50)
        for (item in normalizedList) {
            if (item.second.contains(cleanQuery, ignoreCase = true)) {
                results.add(item.first)
                if (results.size >= 50) break
            }
        }
        return results
    }

    private fun removeArabicDiacritics(text: String): String {
        return text.replace(DIACRITICS_REGEX, "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace('ة', 'ه')
    }

    suspend fun getRandomAyah(): com.example.data.local.AyahEntity? {
        return dao.getRandomAyah()
    }

    suspend fun getRandomShortAyah(maxLength: Int = 100): com.example.data.local.AyahEntity? {
        return dao.getRandomShortAyah(maxLength)
    }

    suspend fun getSurahNameById(id: Int): String? {
        return dao.getSurahNameById(id)
    }

    suspend fun getAyahByNumber(surahId: Int, ayahNumber: Int): AyahEntity? {
        return dao.getAyahByNumber(surahId, ayahNumber)
    }

}
