package com.example.test1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.example.test1.data.repository.SettingsRepository
import com.example.test1.ui.component.UpdateDialog
import com.example.test1.ui.navigation.AppNavigation
import com.example.test1.ui.theme.AppTheme
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val updateInfoState: MutableState<Update?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForUpdates()
        enableEdgeToEdge()

        setContent {
            val themeOption by settingsRepository.themeOptionFlow.collectAsState(initial = "Systemowy")
            val useDarkTheme = when (themeOption) {
                "Jasny" -> false
                "Ciemny" -> true
                else -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = useDarkTheme) {
                // Pobieramy aktualną wartość ze stanu
                val updateInfo by updateInfoState

                AppNavigation()

                updateInfo?.let { update ->
                    UpdateDialog(
                        update = update,
                        onDismiss = { updateInfoState.value = null }
                    )
                }
            }
        }
    }

    private fun checkForUpdates() {
        AppUpdaterUtils(this)
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setGitHubUserAndRepo("MacPal2002", "ans-nt-app_release")
            .withListener(object : AppUpdaterUtils.UpdateListener {
                override fun onSuccess(update: Update, isUpdateAvailable: Boolean?) {
                    // --- POCZĄTEK LOGOWANIA ---
                    Log.d("AppUpdater", "Sprawdzanie aktualizacji zakończone powodzeniem.")
                    Log.d("AppUpdater", "Czy aktualizacja jest dostępna? -> $isUpdateAvailable")
                    Log.d("AppUpdater", "Najnowsza wersja: ${update.latestVersion}")
                    Log.d("AppUpdater", "Opis zmian: ${update.releaseNotes}")
                    Log.d("AppUpdater", "BEZPOŚREDNI LINK DO POBRANIA: ${update.urlToDownload}")
                    if (isUpdateAvailable == true) {
                        updateInfoState.value = update
                    }
                }
                override fun onFailed(error: AppUpdaterError) {
                    Log.e("AppUpdater Error", "Sprawdzanie aktualizacji nie powiodło się: $error")
                }
            })
            .start()
    }
}

