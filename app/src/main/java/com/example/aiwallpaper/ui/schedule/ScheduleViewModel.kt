package com.example.aiwallpaper.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.aiwallpaper.data.model.WallpaperStyle
import com.example.aiwallpaper.storage.EncryptedPreferences
import com.example.aiwallpaper.worker.DailyWallpaperWorker
import com.example.aiwallpaper.worker.RotationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class ScheduleUiState(
    val dailyEnabled: Boolean = false,
    val dailyHour: Int = 7,
    val dailyPrompt: String = "",
    val dailyStyle: WallpaperStyle = WallpaperStyle.AMOLED,
    val rotationEnabled: Boolean = false,
    val rotationIntervalHours: Int = 24,
    val saved: Boolean = false
)

class ScheduleViewModel(private val prefs: EncryptedPreferences) : ViewModel() {

    private val _state = MutableStateFlow(
        ScheduleUiState(
            dailyEnabled = prefs.getDailyEnabled(),
            dailyHour = prefs.getDailyHour(),
            dailyPrompt = prefs.getDailyPrompt(),
            dailyStyle = runCatching { WallpaperStyle.valueOf(prefs.getDailyStyle()) }.getOrDefault(WallpaperStyle.AMOLED),
            rotationEnabled = prefs.getRotationEnabled(),
            rotationIntervalHours = prefs.getRotationIntervalHours()
        )
    )
    val state: StateFlow<ScheduleUiState> = _state

    fun setDailyEnabled(v: Boolean) = _state.update { it.copy(dailyEnabled = v) }
    fun setDailyHour(h: Int) = _state.update { it.copy(dailyHour = h) }
    fun setDailyPrompt(p: String) = _state.update { it.copy(dailyPrompt = p) }
    fun setDailyStyle(s: WallpaperStyle) = _state.update { it.copy(dailyStyle = s) }
    fun setRotationEnabled(v: Boolean) = _state.update { it.copy(rotationEnabled = v) }
    fun setRotationInterval(h: Int) = _state.update { it.copy(rotationIntervalHours = h) }
    fun clearSaved() = _state.update { it.copy(saved = false) }

    fun save(context: Context) {
        val s = _state.value
        prefs.saveDailyEnabled(s.dailyEnabled)
        prefs.saveDailyHour(s.dailyHour)
        prefs.saveDailyPrompt(s.dailyPrompt)
        prefs.saveDailyStyle(s.dailyStyle.name)
        prefs.saveRotationEnabled(s.rotationEnabled)
        prefs.saveRotationIntervalHours(s.rotationIntervalHours)

        val wm = WorkManager.getInstance(context)
        val netConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        if (s.dailyEnabled) {
            val delay = delayUntilHour(s.dailyHour)
            val req = PeriodicWorkRequestBuilder<DailyWallpaperWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(netConstraint)
                .build()
            wm.enqueueUniquePeriodicWork("daily_wallpaper", ExistingPeriodicWorkPolicy.UPDATE, req)
        } else {
            wm.cancelUniqueWork("daily_wallpaper")
        }

        if (s.rotationEnabled) {
            val req = PeriodicWorkRequestBuilder<RotationWorker>(s.rotationIntervalHours.toLong(), TimeUnit.HOURS)
                .build()
            wm.enqueueUniquePeriodicWork("wallpaper_rotation", ExistingPeriodicWorkPolicy.UPDATE, req)
        } else {
            wm.cancelUniqueWork("wallpaper_rotation")
        }

        _state.update { it.copy(saved = true) }
    }

    private fun delayUntilHour(hour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    class Factory(private val prefs: EncryptedPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ScheduleViewModel(prefs) as T
    }
}
