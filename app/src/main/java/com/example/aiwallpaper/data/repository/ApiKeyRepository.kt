package com.example.aiwallpaper.data.repository

import com.example.aiwallpaper.storage.EncryptedPreferences

class ApiKeyRepository(private val prefs: EncryptedPreferences) {

    fun saveApiKey(key: String) = prefs.saveApiKey(key.trim())

    fun getApiKey(): String? = prefs.getApiKey()

    fun hasApiKey(): Boolean = prefs.hasApiKey()

    fun clearApiKey() = prefs.clearApiKey()
}
