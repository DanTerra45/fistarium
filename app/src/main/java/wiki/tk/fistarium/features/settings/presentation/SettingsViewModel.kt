package wiki.tk.fistarium.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import wiki.tk.fistarium.core.preferences.PreferencesManager

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val themeMode = preferencesManager.themeMode
    val appLanguage = preferencesManager.appLanguage

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setAppLanguage(lang)
        }
    }
}
