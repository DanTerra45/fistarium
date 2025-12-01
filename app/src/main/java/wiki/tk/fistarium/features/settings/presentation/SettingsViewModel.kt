package wiki.tk.fistarium.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wiki.tk.fistarium.core.preferences.PreferencesManager

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Properly encapsulated as StateFlow with initial values
    val themeMode: StateFlow<Int> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val appLanguage: StateFlow<String> = preferencesManager.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    // Error state for user feedback
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                preferencesManager.setThemeMode(mode)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to save theme preference: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                preferencesManager.setAppLanguage(lang)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to save language preference: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
