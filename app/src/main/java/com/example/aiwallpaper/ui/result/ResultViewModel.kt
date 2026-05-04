package com.example.aiwallpaper.ui.result

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aiwallpaper.data.model.WallpaperHistory
import com.example.aiwallpaper.data.repository.WallpaperRepository
import com.example.aiwallpaper.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultUiState(
    val history: WallpaperHistory? = null,
    val isLoading: Boolean = true,
    val toast: String? = null        // one-shot snackbar message
)

class ResultViewModel(private val repo: WallpaperRepository) : ViewModel() {

    private val _state = MutableStateFlow(ResultUiState())
    val state: StateFlow<ResultUiState> = _state

    fun load(id: Long) {
        viewModelScope.launch {
            val item = repo.getHistoryById(id)
            _state.update { it.copy(history = item, isLoading = false) }
        }
    }

    fun downloadToGallery(context: Context) {
        val path = _state.value.history?.imagePath ?: return
        viewModelScope.launch {
            val uri = ImageUtils.saveToGallery(context, path, "ai_wallpaper_${System.currentTimeMillis()}")
            _state.update {
                it.copy(toast = if (uri != null) "Saved to gallery!" else "Couldn't save image")
            }
        }
    }

    fun setAsWallpaper(context: Context) {
        val path = _state.value.history?.imagePath ?: return
        viewModelScope.launch {
            val ok = ImageUtils.setAsWallpaper(context, path)
            _state.update { it.copy(toast = if (ok) "Wallpaper set!" else "Couldn't set wallpaper") }
        }
    }

    fun clearToast() {
        _state.update { it.copy(toast = null) }
    }

    class Factory(private val repo: WallpaperRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ResultViewModel(repo) as T
    }
}
