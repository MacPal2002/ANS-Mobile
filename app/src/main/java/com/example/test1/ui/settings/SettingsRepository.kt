package com.example.test1.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repozytorium do zarządzania lokalnymi ustawieniami aplikacji.
 * Odpowiada za komunikację z DataStore.
 * @param dataStore Bezpośrednia referencja do instancji DataStore.
 */
class SettingsRepository(
    // ZMIANA: Zamiast Context, przyjmujemy bezpośrednio DataStore
    private val dataStore: DataStore<Preferences>
) {

    private val themePreferenceKey = stringPreferencesKey("theme_option")

    val themeOptionFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[themePreferenceKey] ?: "Systemowy"
        }

    suspend fun saveThemeOption(theme: String) {
        dataStore.edit { settings ->
            settings[themePreferenceKey] = theme
        }
    }
}
