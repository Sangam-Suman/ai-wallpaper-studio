package com.example.aiwallpaper.data.repository

import com.example.aiwallpaper.data.model.*
import com.example.aiwallpaper.network.NetworkModule
import com.example.aiwallpaper.storage.EncryptedPreferences
import com.example.aiwallpaper.storage.HistoryDao
import kotlinx.coroutines.flow.Flow

class WallpaperRepository(
    private val encryptedPrefs: EncryptedPreferences,
    private val historyDao: HistoryDao
) {

    // --------------- API Generation ---------------

    suspend fun generateWallpaper(prompt: String, style: WallpaperStyle): Result<String> {
        val apiKey = encryptedPrefs.getApiKey()
            ?: return Result.failure(Exception("AI Connection not configured. Please reconnect in Settings."))

        val fullPrompt = buildPrompt(prompt, style)
        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))),
            generationConfig = GeminiGenerationConfig()
        )

        return try {
            val response = NetworkModule.geminiService.generateImage(apiKey = apiKey, body = request)

            if (response.isSuccessful) {
                // Find the part that contains image data (may be alongside a text part)
                val imageBase64 = response.body()
                    ?.candidates?.firstOrNull()
                    ?.content?.parts
                    ?.firstOrNull { it.inlineData != null }
                    ?.inlineData?.data

                if (imageBase64 != null) {
                    Result.success(imageBase64)
                } else {
                    Result.failure(Exception("The AI didn't return an image. Try a different prompt."))
                }
            } else {
                val mapped = when (response.code()) {
                    400 -> "Invalid request. Try rephrasing your prompt."
                    401, 403 -> "Your AI Connection key is invalid or expired. Please reconnect."
                    429 -> "Too many requests. Wait a moment and try again."
                    500, 503 -> "Google's AI servers are busy. Try again in a bit."
                    else -> "Something went wrong (Error ${response.code()}). Try again."
                }
                Result.failure(Exception(mapped))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message ?: "Check your internet connection."}"))
        }
    }

    // --------------- History ---------------

    suspend fun saveToHistory(prompt: String, style: WallpaperStyle, imagePath: String): Long {
        return historyDao.insert(
            WallpaperHistory(prompt = prompt, style = style.name, imagePath = imagePath)
        )
    }

    fun getHistory(): Flow<List<WallpaperHistory>> = historyDao.getAllHistory()

    suspend fun getHistoryById(id: Long): WallpaperHistory? = historyDao.getById(id)

    suspend fun deleteHistory(history: WallpaperHistory) = historyDao.delete(history)

    // --------------- Helpers ---------------

    private fun buildPrompt(userPrompt: String, style: WallpaperStyle): String =
        "Create a stunning smartphone wallpaper in portrait 9:16 aspect ratio. " +
                "Style: ${style.promptSuffix}. " +
                "Content: $userPrompt. " +
                "Make it visually striking, high resolution, suitable as a phone home-screen wallpaper."
}
