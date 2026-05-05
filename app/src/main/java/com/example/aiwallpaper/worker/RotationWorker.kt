package com.example.aiwallpaper.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aiwallpaper.storage.AppDatabase
import com.example.aiwallpaper.utils.ImageUtils

class RotationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val history = AppDatabase.getInstance(applicationContext).historyDao().getAllHistoryList()
        if (history.isEmpty()) return Result.success()
        val item = history.random()
        val ok = ImageUtils.setAsWallpaper(applicationContext, item.imagePath)
        return if (ok) Result.success() else Result.retry()
    }
}
