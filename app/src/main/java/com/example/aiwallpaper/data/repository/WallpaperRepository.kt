package com.example.aiwallpaper.data.repository

import android.util.Base64
import com.example.aiwallpaper.data.model.*
import com.example.aiwallpaper.network.NetworkModule
import com.example.aiwallpaper.storage.EncryptedPreferences
import com.example.aiwallpaper.storage.HistoryDao
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.URLEncoder
import kotlin.random.Random

class WallpaperRepository(
    private val encryptedPrefs: EncryptedPreferences,
    private val historyDao: HistoryDao
) {
    private val gson = Gson()

    // --------------- Provider preference ---------------

    fun getProvider(): ImageProvider = encryptedPrefs.getProvider()

    fun saveProvider(provider: ImageProvider) = encryptedPrefs.saveProvider(provider)

    // --------------- API Generation ---------------

    suspend fun generateWallpaper(prompt: String, style: WallpaperStyle, provider: ImageProvider): Result<String> {
        return when (provider) {
            ImageProvider.GEMINI -> generateWithGemini(prompt, style)
            ImageProvider.POLLINATIONS -> generateWithPollinations(prompt, style)
        }
    }

    private suspend fun generateWithGemini(prompt: String, style: WallpaperStyle): Result<String> {
        val apiKey = encryptedPrefs.getApiKey()
            ?: return Result.failure(Exception("AI Connection not configured. Please reconnect in Settings."))

        val fullPrompt = buildPrompt(prompt, style)
        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))),
            generationConfig = GeminiGenerationConfig()
        )

        val retryDelaysMs = listOf(8_000L, 20_000L)

        for (attempt in 0..retryDelaysMs.size) {
            try {
                val response = NetworkModule.geminiService.generateImage(apiKey = apiKey, body = request)

                if (response.isSuccessful) {
                    val imageBase64 = response.body()
                        ?.candidates?.firstOrNull()
                        ?.content?.parts
                        ?.firstOrNull { it.inlineData != null }
                        ?.inlineData?.data

                    return if (imageBase64 != null) {
                        Result.success(imageBase64)
                    } else {
                        Result.failure(Exception("The AI didn't return an image. Try a different prompt."))
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    val apiError = parseApiError(errorBody)

                    when (response.code()) {
                        429 -> {
                            val isQuotaExhausted = apiError?.status == "RESOURCE_EXHAUSTED"
                                    || apiError?.message?.contains("quota", ignoreCase = true) == true
                            if (isQuotaExhausted) {
                                return Result.failure(Exception(QUOTA_EXHAUSTED_MARKER))
                            }
                            if (attempt < retryDelaysMs.size) {
                                delay(retryDelaysMs[attempt])
                                continue
                            }
                            return Result.failure(Exception("Rate limit hit after retries. Wait a minute and try again."))
                        }
                        400 -> return Result.failure(Exception("Invalid request. Try rephrasing your prompt."))
                        401, 403 -> return Result.failure(Exception("Your AI Connection key is invalid or expired. Please reconnect."))
                        500, 503 -> {
                            if (attempt < retryDelaysMs.size) {
                                delay(retryDelaysMs[attempt])
                                continue
                            }
                            return Result.failure(Exception("Google's AI servers are busy. Try again in a bit."))
                        }
                        else -> return Result.failure(Exception("Something went wrong (Error ${response.code()}). Try again."))
                    }
                }
            } catch (e: Exception) {
                if (attempt < retryDelaysMs.size) {
                    delay(retryDelaysMs[attempt])
                    continue
                }
                return Result.failure(Exception("Network error: ${e.message ?: "Check your internet connection."}"))
            }
        }
        return Result.failure(Exception("Request failed after retries. Check your connection and try again."))
    }

    private suspend fun generateWithPollinations(prompt: String, style: WallpaperStyle): Result<String> {
        val fullPrompt = buildPrompt(prompt, style)
        val encoded = URLEncoder.encode(fullPrompt, "UTF-8")
        val seed = Random.nextInt(1, 999_999)
        val url = "https://image.pollinations.ai/prompt/$encoded" +
                "?width=1080&height=1920&nologo=true&enhance=true&seed=$seed&model=flux"

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = NetworkModule.okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                        ?: return@withContext Result.failure(Exception("No image data received. Try again."))
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    Result.success(base64)
                } else {
                    Result.failure(Exception("Free image service error (${response.code}). Try again."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error: ${e.message ?: "Check your internet connection."}"))
            }
        }
    }

    private fun parseApiError(errorBody: String?): GeminiApiError? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val error = gson.fromJson(errorBody, JsonObject::class.java)
                ?.getAsJsonObject("error") ?: return null
            GeminiApiError(
                code = error.get("code")?.asInt ?: 0,
                message = error.get("message")?.asString ?: "",
                status = error.get("status")?.asString ?: ""
            )
        } catch (_: Exception) {
            null
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

    companion object {
        // Sentinel used by ViewModel to detect quota exhaustion and auto-fallback
        const val QUOTA_EXHAUSTED_MARKER = "__QUOTA_EXHAUSTED__"
    }
}
