package com.example.khatma

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class KhatmaRepository(
    private val dao: KhatmaDao,
    private val context: Context? = null
) {
    fun observeAllJuz(): Flow<List<KhatmaProgress>> = dao.observeAllJuz()

    suspend fun ensureInitializedAndRestored(): Unit = withContext(Dispatchers.IO) {
        val dbMap = dao.getAllJuzList().associateBy { it.juzNumber }
        val backupMap = context?.let { ctx ->
            KhatmaBackupManager.loadBackup(ctx).associateBy { it.juzNumber }
        } ?: emptyMap()

        val mergedList = mutableListOf<KhatmaProgress>()
        for (juz in 1..30) {
            val dbItem = dbMap[juz]
            val backupItem = backupMap[juz]
            val merged = when {
                dbItem != null && backupItem != null -> {
                    val bestPercent = maxOf(dbItem.progressPercent, backupItem.progressPercent)
                    val bestPage = maxOf(dbItem.lastReadPage, backupItem.lastReadPage)
                    val completed = dbItem.isCompleted || backupItem.isCompleted
                    val bestTime = maxOf(dbItem.lastUpdatedAt, backupItem.lastUpdatedAt)
                    KhatmaProgress(
                        juzNumber = juz,
                        lastReadPage = bestPage,
                        progressPercent = bestPercent,
                        isCompleted = completed,
                        lastUpdatedAt = if (bestTime > 0L) bestTime else System.currentTimeMillis()
                    )
                }
                dbItem != null -> dbItem
                backupItem != null -> backupItem
                else -> KhatmaProgress(
                    juzNumber = juz,
                    lastReadPage = 0,
                    progressPercent = 0,
                    isCompleted = false,
                    lastUpdatedAt = System.currentTimeMillis()
                )
            }
            mergedList.add(merged)
        }

        dao.insertAll(mergedList)
        context?.let { KhatmaBackupManager.saveBackup(it, mergedList) }
    }

    suspend fun updateProgress(currentPage: Int): Unit = withContext(Dispatchers.IO) {
        val juz = JuzPageMapping.juzOf(currentPage)
        val percent = JuzPageMapping.progressPercent(juz, currentPage)
        val completed = percent >= 100
        val existing = dao.getJuz(juz)

        val bestPercent = maxOf(percent, existing?.progressPercent ?: 0)
        val bestPage = maxOf(currentPage, existing?.lastReadPage ?: 0)

        val updated = KhatmaProgress(
            juzNumber = juz,
            lastReadPage = bestPage,
            progressPercent = bestPercent,
            isCompleted = completed || (existing?.isCompleted == true),
            lastUpdatedAt = System.currentTimeMillis()
        )
        dao.upsert(updated)
        context?.let { KhatmaBackupManager.saveSingleJuz(it, updated) }
    }

    suspend fun getNextJuzToOpen(): Int = dao.getFirstIncompleteJuz() ?: 1

    suspend fun getJuz(juz: Int): KhatmaProgress? = dao.getJuz(juz)

    suspend fun startNewKhatma(): Unit = withContext(Dispatchers.IO) {
        dao.resetAll()
        context?.let { ctx ->
            val freshList = (1..30).map { KhatmaProgress(juzNumber = it) }
            KhatmaBackupManager.saveBackup(ctx, freshList)
        }
    }

    suspend fun resetJuz(juz: Int): Unit = withContext(Dispatchers.IO) {
        val resetItem = KhatmaProgress(
            juzNumber = juz,
            lastReadPage = 0,
            progressPercent = 0,
            isCompleted = false,
            lastUpdatedAt = System.currentTimeMillis()
        )
        dao.upsert(resetItem)
        context?.let { KhatmaBackupManager.saveSingleJuz(it, resetItem) }
    }
}
