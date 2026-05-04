package com.example.aiwallpaper.network

import com.example.aiwallpaper.data.model.GeminiRequest
import com.example.aiwallpaper.data.model.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    // The user's API key is passed as a query param so it goes directly to Google's servers.
    // We never route it through any backend — billing stays on the user's account.
    @POST("v1beta/models/gemini-2.5-flash-image:generateContent")
    suspend fun generateImage(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): Response<GeminiResponse>
}
