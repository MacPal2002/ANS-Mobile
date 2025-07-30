package com.example.test1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test1.ui.navigation.AppNavigation
import com.example.test1.ui.settings.SettingsRepository
import com.example.test1.ui.settings.SettingsViewModel
import com.example.test1.ui.settings.SettingsViewModelFactory
import com.example.test1.ui.settings.settingsDataStore
import com.example.test1.ui.theme.AppTheme
import android.app.Application
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Utwórz instancję repozytorium, aby mieć dostęp do DataStore
        val settingsRepository = SettingsRepository(this.settingsDataStore)

        setContent {
            // 2. Pobierz opcję motywu z repozytorium jako stan
            val themeOption by settingsRepository.themeOptionFlow.collectAsState(initial = "Systemowy")

            // 3. Ustal, czy włączyć tryb ciemny na podstawie wyboru użytkownika
            val useDarkTheme = when (themeOption) {
                "Jasny" -> false
                "Ciemny" -> true
                else -> isSystemInDarkTheme() // Dla opcji "Systemowy"
            }

            // 4. Przekaż właściwą decyzję (true/false) do AppTheme
            AppTheme(darkTheme = useDarkTheme) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(
                        application = (LocalContext.current.applicationContext as Application),
                        repository = settingsRepository)
                )
                AppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}