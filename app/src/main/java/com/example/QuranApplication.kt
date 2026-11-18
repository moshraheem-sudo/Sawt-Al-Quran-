package com.example

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.QuranDatabase
import com.example.data.remote.RetrofitInstance
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuranApplication : Application() {

    lateinit var repository: QuranRepository
        private set
    lateinit var khatmaRepository: com.example.khatma.KhatmaRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `khatma_progress` (`juzNumber` INTEGER NOT NULL, `lastReadPage` INTEGER NOT NULL, `progressPercent` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `lastUpdatedAt` INTEGER NOT NULL, PRIMARY KEY(`juzNumber`))")
                // Insert default 30 Juz
                for (i in 1..30) {
                    db.execSQL("INSERT OR IGNORE INTO `khatma_progress` (juzNumber, lastReadPage, progressPercent, isCompleted, lastUpdatedAt) VALUES ($i, 0, 0, 0, 0)")
                }
            }
        }

        val dbCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                for (i in 1..30) {
                    db.execSQL("INSERT OR IGNORE INTO `khatma_progress` (juzNumber, lastReadPage, progressPercent, isCompleted, lastUpdatedAt) VALUES ($i, 0, 0, 0, 0)")
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                for (i in 1..30) {
                    db.execSQL("INSERT OR IGNORE INTO `khatma_progress` (juzNumber, lastReadPage, progressPercent, isCompleted, lastUpdatedAt) VALUES ($i, 0, 0, 0, 0)")
                }
            }
        }

        val db = Room.databaseBuilder(
            applicationContext,
            QuranDatabase::class.java,
            "quran.db"
        )
            .addMigrations(MIGRATION_1_2)
            .addCallback(dbCallback)
            .fallbackToDestructiveMigration()
            .build()

        repository = QuranRepository(db.quranDao(), RetrofitInstance.api)
        khatmaRepository = com.example.khatma.KhatmaRepository(db.khatmaDao(), applicationContext)
        com.example.data.local.HijriCalendarManager.init(applicationContext)
        if (com.example.data.local.NotificationSettingsManager.areNotificationsEnabled(applicationContext)) {
            com.example.notification.NotificationScheduler.schedule(
                applicationContext,
                forceWorkUpdate = false
            )
        }

        applicationScope.launch {
            try {
                khatmaRepository.ensureInitializedAndRestored()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                com.example.data.local.HijriCalendarManager.refreshFromRemote(
                    context = applicationContext,
                    force = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
