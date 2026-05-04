package com.example.aiwallpaper.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aiwallpaper.data.repository.ApiKeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class OnboardingUiState(
    val step: Int = 0,             // 0 = welcome, 1 = get key, 2 = enter key
    val keyInput: String = "",
    val isValidating: Boolean = false,
    val error: String? = null
)

class OnboardingViewModel(private val repo: ApiKeyRepository) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state

    fun onKeyInputChanged(value: String) {
        _state.update { it.copy(keyInput = value, error = null) }
    }

    fun nextStep() {
        _state.update { it.copy(step = it.step + 1) }
    }

    fun connectAI(onSuccess: () -> Unit) {
        val key = _state.value.keyInput.trim()

        if (key.isBlank()) {
            _state.update { it.copy(error = "Please paste your key first") }
            return
        }
        if (key.length < 20) {
            _state.update { it.copy(error = "That doesn't look right. Keys are usually longer — double-check it.") }
            return
        }

        // Save the key; actual validity is confirmed on first generation attempt
        repo.saveApiKey(key)
        onSuccess()
    }

    class Factory(private val repo: ApiKeyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OnboardingViewModel(repo) as T
    }
}
