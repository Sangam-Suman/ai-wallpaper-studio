package com.example.aiwallpaper.ui.generator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aiwallpaper.data.model.ImageProvider
import com.example.aiwallpaper.data.model.WallpaperStyle
import com.example.aiwallpaper.data.repository.WallpaperRepository
import com.example.aiwallpaper.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeneratorUiState(
    val prompt: String = "",
    val selectedStyle: WallpaperStyle = WallpaperStyle.AMOLED,
    val provider: ImageProvider = ImageProvider.GEMINI,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val navigateToHistoryId: Long? = null
)

class GeneratorViewModel(private val repo: WallpaperRepository) : ViewModel() {

    private val _state = MutableStateFlow(GeneratorUiState(provider = repo.getProvider()))
    val state: StateFlow<GeneratorUiState> = _state

    fun onPromptChanged(text: String) {
        _state.update { it.copy(prompt = text, error = null) }
    }

    fun onStyleSelected(style: WallpaperStyle) {
        _state.update { it.copy(selectedStyle = style) }
    }

    fun onProviderSelected(provider: ImageProvider) {
        _state.update { it.copy(provider = provider, error = null) }
        repo.saveProvider(provider)
    }

    fun generate(context: Context) {
        val current = _state.value
        if (current.prompt.isBlank()) {
            _state.update { it.copy(error = "Describe what you want to see") }
            return
        }

        _state.update { it.copy(isGenerating = true, error = null) }

        viewModelScope.launch {
            val result = repo.generateWallpaper(current.prompt, current.selectedStyle, current.provider)

            // Auto-fallback: Gemini quota exhausted → silently switch to free FLUX and retry
            val finalResult = if (result.isFailure
                && current.provider == ImageProvider.GEMINI
                && result.exceptionOrNull()?.message == WallpaperRepository.QUOTA_EXHAUSTED_MARKER
            ) {
                _state.update { it.copy(provider = ImageProvider.POLLINATIONS) }
                repo.saveProvider(ImageProvider.POLLINATIONS)
                repo.generateWallpaper(current.prompt, current.selectedStyle, ImageProvider.POLLINATIONS)
            } else {
                result
            }

            finalResult.fold(
                onSuccess = { base64 ->
                    val fileName = "wallpaper_${System.currentTimeMillis()}"
                    val path = ImageUtils.saveImageToInternalStorage(context, base64, fileName)
                    if (path != null) {
                        val id = repo.saveToHistory(current.prompt, current.selectedStyle, path)
                        _state.update { it.copy(isGenerating = false, navigateToHistoryId = id) }
                    } else {
                        _state.update { it.copy(isGenerating = false, error = "Couldn't save the image. Try again.") }
                    }
                },
                onFailure = { e ->
                    val message = if (e.message == WallpaperRepository.QUOTA_EXHAUSTED_MARKER) {
                        "Free API quota used up for today. It resets at midnight Pacific Time."
                    } else {
                        e.message
                    }
                    _state.update { it.copy(isGenerating = false, error = message) }
                }
            )
        }
    }

    fun onNavigationConsumed() {
        _state.update { it.copy(navigateToHistoryId = null) }
    }

    class Factory(private val repo: WallpaperRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GeneratorViewModel(repo) as T
    }
}
