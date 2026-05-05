package com.example.aiwallpaper.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.aiwallpaper.data.model.ImageProvider
import com.example.aiwallpaper.data.model.WallpaperStyle

class EncryptedPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "ai_wallpaper_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String) = prefs.edit().putString(KEY_API, key).apply()

    fun getApiKey(): String? = prefs.getString(KEY_API, null)

    fun clearApiKey() = prefs.edit().remove(KEY_API).apply()

    fun hasApiKey(): Boolean = !getApiKey().isNullOrBlank()

    fun saveProvider(provider: ImageProvider) =
        prefs.edit().putString(KEY_PROVIDER, provider.name).apply()

    fun getProvider(): ImageProvider =
        ImageProvider.entries.find { it.name == prefs.getString(KEY_PROVIDER, null) }
            ?: ImageProvider.GEMINI

    fun saveDailyEnabled(v: Boolean) = prefs.edit().putBoolean(KEY_DAILY_ON, v).apply()
    fun getDailyEnabled(): Boolean = prefs.getBoolean(KEY_DAILY_ON, false)
    fun saveDailyHour(h: Int) = prefs.edit().putInt(KEY_DAILY_HOUR, h).apply()
    fun getDailyHour(): Int = prefs.getInt(KEY_DAILY_HOUR, 7)
    fun saveDailyPrompt(p: String) = prefs.edit().putString(KEY_DAILY_PROMPT, p).apply()
    fun getDailyPrompt(): String = prefs.getString(KEY_DAILY_PROMPT, null) ?: "Cyberpunk Tokyo rain-soaked street at night, neon signs, reflections, fog"
    fun saveDailyStyle(s: String) = prefs.edit().putString(KEY_DAILY_STYLE, s).apply()
    fun getDailyStyle(): String = prefs.getString(KEY_DAILY_STYLE, null) ?: WallpaperStyle.AMOLED.name
    fun saveRotationEnabled(v: Boolean) = prefs.edit().putBoolean(KEY_ROT_ON, v).apply()
    fun getRotationEnabled(): Boolean = prefs.getBoolean(KEY_ROT_ON, false)
    fun saveRotationIntervalHours(h: Int) = prefs.edit().putInt(KEY_ROT_INTERVAL, h).apply()
    fun getRotationIntervalHours(): Int = prefs.getInt(KEY_ROT_INTERVAL, 24)

    companion object {
        private const val KEY_API = "gemini_api_key"
        private const val KEY_PROVIDER = "image_provider"
        private const val KEY_DAILY_ON = "daily_enabled"
        private const val KEY_DAILY_HOUR = "daily_hour"
        private const val KEY_DAILY_PROMPT = "daily_prompt"
        private const val KEY_DAILY_STYLE = "daily_style"
        private const val KEY_ROT_ON = "rotation_enabled"
        private const val KEY_ROT_INTERVAL = "rotation_interval_hours"
    }
}
