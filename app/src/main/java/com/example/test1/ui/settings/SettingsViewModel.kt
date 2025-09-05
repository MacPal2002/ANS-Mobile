package com.example.test1.ui.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.example.test1.data.repository.ScheduleRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.test1.data.repository.SettingsRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduleRepository: ScheduleRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val installations: FirebaseInstallations
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private var groupsListenerJob: Job? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser != null) {
            loadInitialUserData()
            listenForObservedGroupsChanges()
        } else {
            groupsListenerJob?.cancel()
            _uiState.update { currentState ->
                SettingsState(themeOption = currentState.themeOption)
            }
        }
    }

    init {
        collectTheme()
        auth.addAuthStateListener(authStateListener)
    }

    private fun loadInitialUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = auth.currentUser?.uid ?: return@launch

            try {
                // Używamy JEDNEGO sposobu pobrania ID - await()
                val deviceId = installations.id.await()

                val userDoc = firestore.collection("students").document(userId).get().await()
                if (userDoc.exists()) {
                    val devicesMap = userDoc.get("devices") as? Map<String, Any>
                    val deviceSettings = devicesMap?.get(deviceId) as? Map<String, Any>

                    val notificationsEnabled = deviceSettings?.get("notificationEnabled") as? Boolean ?: false
                    val notificationTime = deviceSettings?.get("notificationTimeOption") as? String ?: "N/A"

                    _uiState.update {
                        it.copy(
                            displayName = userDoc.getString("displayName") ?: "Brak nazwy",
                            albumNumber = userDoc.getString("albumNumber") ?: "Brak albumu",
                            notificationsEnabled = notificationsEnabled,
                            notificationTimeOption = notificationTime,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Nie znaleziono danych użytkownika.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }


    private fun listenForObservedGroupsChanges() {
        // KROK 2: Anuluj poprzednie zadanie i zapisz nowe
        groupsListenerJob?.cancel()
        groupsListenerJob = viewModelScope.launch {
            scheduleRepository.getObservedGroupsFlow().collect { result ->
                result.onSuccess { groups ->
                    _uiState.update {
                        it.copy(observedGroups = groups.map { g -> g.name }.ifEmpty { listOf("Brak") })
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            }
        }
    }

    // Prywatna funkcja do zbierania motywu, dla czystości kodu
    private fun collectTheme() {
        viewModelScope.launch {
            settingsRepository.themeOptionFlow.collect { theme ->
                _uiState.update { it.copy(themeOption = theme) }
            }
        }
    }

    private fun updateDeviceSetting(key: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val deviceId = installations.id.await()
                val settingPath = "devices.$deviceId.$key"
                firestore.collection("students").document(userId).update(settingPath, value).await()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Błąd podczas aktualizacji ustawienia urządzenia", e)
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onThemeChange(theme: String) {
        viewModelScope.launch {
            settingsRepository.saveThemeOption(theme)
        }
    }

    fun onNotificationTimeChange(time: String) {
        // ZMIANA: Używamy nowej funkcji, aby zapisać ustawienie dla tego urządzenia
        _uiState.update { it.copy(notificationTimeOption = time) }
        updateDeviceSetting("notificationTimeOption", time)
    }
    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        updateDeviceSetting("notificationEnabled", enabled)
    }

    fun onNotificationToggleRequested(
        isChecked: Boolean,
        requestPermission: () -> Unit,
    ) {
        if (isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission()
            } else {
                // Na starszych wersjach po prostu włączamy
                updateDeviceSetting("notificationEnabled", true)
                _uiState.update { it.copy(notificationsEnabled = true) }
            }
        } else {
            updateDeviceSetting("notificationEnabled", false)
            _uiState.update { it.copy(notificationsEnabled = false) }
        }
    }

    fun onLogout() {
        val userId = auth.currentUser?.uid ?: run {
            auth.signOut() // Jeśli nie ma usera, po prostu wyloguj
            return
        }

        viewModelScope.launch {
            try {
                val deviceId = installations.id.await()
                val deviceField = FieldValue.delete()
                firestore.collection("students").document(userId)
                    .update("devices.$deviceId", deviceField).await()
                Log.d("Logout", "Dane urządzenia pomyślnie usunięte.")
            } catch (e: Exception) {
                Log.w("Logout", "Błąd podczas usuwania danych urządzenia, ale i tak wylogowuję", e)
            } finally {
                // Niezależnie od tego, czy usunięcie danych się udało, czy nie, wyloguj
                auth.signOut()
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    // WAŻNE: Usuwanie listenera, gdy ViewModel jest niszczony, aby uniknąć wycieków pamięci.
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        groupsListenerJob?.cancel()
    }
}