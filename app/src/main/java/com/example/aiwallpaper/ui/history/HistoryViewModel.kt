package com.example.aiwallpaper.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aiwallpaper.data.model.WallpaperHistory
import com.example.aiwallpaper.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repo: WallpaperRepository) : ViewModel() {

    val history: StateFlow<List<WallpaperHistory>> = repo.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(item: WallpaperHistory) {
        viewModelScope.launch { repo.deleteHistory(item) }
    }

    class Factory(private val repo: WallpaperRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HistoryViewModel(repo) as T
    }
}
