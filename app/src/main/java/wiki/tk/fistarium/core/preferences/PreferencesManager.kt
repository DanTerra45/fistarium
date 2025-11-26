package wiki.tk.fistarium.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val THEME_MODE = intPreferencesKey("theme_mode") // 0: System, 1: Light, 2: Dark
        val APP_LANGUAGE = stringPreferencesKey("app_language") // "en", "es"
    }

    val themeMode: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: 0
        }

    val appLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LANGUAGE] ?: "en-US"
        }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = lang
        }
    }
}
