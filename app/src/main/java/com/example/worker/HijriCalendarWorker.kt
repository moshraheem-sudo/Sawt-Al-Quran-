package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.HijriCalendarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HijriCalendarWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val result = HijriCalendarManager.refreshFromRemote(context, force = true)
        if (result.isSuccess) Result.success() else Result.retry()
    }
}
