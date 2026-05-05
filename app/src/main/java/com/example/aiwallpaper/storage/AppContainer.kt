package com.example.aiwallpaper.storage

import android.content.Context
import com.example.aiwallpaper.data.repository.ApiKeyRepository
import com.example.aiwallpaper.data.repository.WallpaperRepository

// Simple service locator — no Hilt to keep MVP lean
object AppContainer {

    private lateinit var encryptedPrefs: EncryptedPreferences
    private lateinit var database: AppDatabase

    fun initialize(context: Context) {
        encryptedPrefs = EncryptedPreferences(context)
        database = AppDatabase.getInstance(context)
    }

    private val _apiKeyRepo: ApiKeyRepository by lazy { ApiKeyRepository(encryptedPrefs) }
    private val _wallpaperRepo: WallpaperRepository by lazy { WallpaperRepository(encryptedPrefs, database.historyDao()) }

    fun apiKeyRepository() = _apiKeyRepo

    fun wallpaperRepository() = _wallpaperRepo

    fun encryptedPrefs(): EncryptedPreferences = encryptedPrefs
}
