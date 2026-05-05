package com.example.aiwallpaper.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.aiwallpaper.data.model.ImageProvider

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

    companion object {
        private const val KEY_API = "gemini_api_key"
        private const val KEY_PROVIDER = "image_provider"
    }
}
