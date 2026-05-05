package com.example.aiwallpaper.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aiwallpaper.data.model.WallpaperStyle
import com.example.aiwallpaper.data.repository.WallpaperRepository
import com.example.aiwallpaper.storage.AppDatabase
import com.example.aiwallpaper.storage.EncryptedPreferences
import com.example.aiwallpaper.utils.ImageUtils

class DailyWallpaperWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = EncryptedPreferences(applicationContext)
        val prompt = prefs.getDailyPrompt()
        val style = runCatching { WallpaperStyle.valueOf(prefs.getDailyStyle()) }.getOrDefault(WallpaperStyle.AMOLED)
        val provider = prefs.getProvider()
        val repo = WallpaperRepository(prefs, AppDatabase.getInstance(applicationContext).historyDao())

        val result = repo.generateWallpaper(prompt, style, provider)
        return result.fold(
            onSuccess = { base64 ->
                val fileName = "daily_${System.currentTimeMillis()}"
                val path = ImageUtils.saveImageToInternalStorage(applicationContext, base64, fileName)
                if (path != null) {
                    repo.saveToHistory(prompt, style, path)
                    ImageUtils.setAsWallpaper(applicationContext, path)
                    Result.success()
                } else {
                    Result.retry()
                }
            },
            onFailure = { Result.retry() }
        )
    }
}
