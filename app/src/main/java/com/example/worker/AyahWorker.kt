package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notification.AyahNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AyahWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val success = AyahNotificationHelper.showRandomAyahNotification(context)
        if (success) Result.success() else Result.retry()
    }
}
