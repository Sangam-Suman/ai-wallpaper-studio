package com.example.aiwallpaper.data.model


// --- Request ---

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

data class GeminiGenerationConfig(
    val responseModalities: List<String> = listOf("IMAGE", "TEXT")
)

// --- Response ---

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val error: GeminiApiError?
)

data class GeminiCandidate(
    val content: GeminiResponseContent?,
    val finishReason: String?
)

data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>?,
    val role: String?
)

data class GeminiResponsePart(
    val text: String?,
    val inlineData: GeminiInlineData?
)

data class GeminiApiError(
    val code: Int,
    val message: String,
    val status: String
)
