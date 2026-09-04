package com.example.khatma

import kotlinx.coroutines.flow.Flow

class KhatmaRepository(private val dao: KhatmaDao) {
    fun observeAllJuz(): Flow<List<KhatmaProgress>> = dao.observeAllJuz()

    suspend fun updateProgress(currentPage: Int) {
        val juz = JuzPageMapping.juzOf(currentPage)
        val percent = JuzPageMapping.progressPercent(juz, currentPage)
        val completed = percent >= 100
        val existing = dao.getJuz(juz)

        val bestPercent = maxOf(percent, existing?.progressPercent ?: 0)
        val bestPage = maxOf(currentPage, existing?.lastReadPage ?: 0)

        dao.upsert(
            KhatmaProgress(
                juzNumber = juz,
                lastReadPage = bestPage,
                progressPercent = bestPercent,
                isCompleted = completed || (existing?.isCompleted == true)
            )
        )
    }

    suspend fun getNextJuzToOpen(): Int = dao.getFirstIncompleteJuz() ?: 1

    suspend fun getJuz(juz: Int): KhatmaProgress? = dao.getJuz(juz)

    suspend fun startNewKhatma() = dao.resetAll()

    suspend fun resetJuz(juz: Int) {
        dao.upsert(KhatmaProgress(juzNumber = juz, lastReadPage = 0, progressPercent = 0, isCompleted = false))
    }
}
