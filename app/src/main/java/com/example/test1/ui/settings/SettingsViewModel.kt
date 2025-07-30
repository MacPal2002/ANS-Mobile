package com.example.test1.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.ui.schedule.ScheduleRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.provider.Settings.Secure
import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.installations.installations

class SettingsViewModel(
    private val application: Application,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val scheduleRepository = ScheduleRepository()
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

            // ZMIANA: Używamy Firebase Installations ID do odczytu
            Firebase.installations.id.addOnSuccessListener { deviceId ->
                viewModelScope.launch {
                    try {
                        val userDoc = db.collection("students").document(userId).get().await()
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
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                }
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
                    _uiState.update { it.copy(error = error.message) }
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

    // NOWA FUNKCJA POMOCNICZA do aktualizacji ustawień per-urządzenie
    private fun updateDeviceSetting(key: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        Firebase.installations.id.addOnSuccessListener { deviceId ->
            val settingPath = "devices.$deviceId.$key"
            db.collection("students").document(userId).update(settingPath, value)
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

    fun onNotificationsToggle(enabled: Boolean) {
        // ZMIANA: Używamy nowej funkcji
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        updateDeviceSetting("notificationEnabled", enabled)
    }

    fun onLogout() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Używamy Firebase Installations ID, aby upewnić się,
            // że usuwamy dane poprawnego urządzenia.
            Firebase.installations.id.addOnSuccessListener { deviceId ->
                // FieldValue.delete() to specjalny obiekt, który usuwa pole z dokumentu
                val deviceField = FieldValue.delete()

                db.collection("students").document(userId)
                    // Usuwamy całą mapę dla tego urządzenia
                    .update("devices.$deviceId", deviceField)
                    .addOnSuccessListener {
                        Log.d("Logout", "Dane urządzenia (w tym token) pomyślnie usunięte.")
                        // Po pomyślnym usunięciu, wylogowujemy użytkownika
                        auth.signOut()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Logout", "Błąd podczas usuwania danych urządzenia", e)
                        // Mimo błędu, nadal wylogowujemy użytkownika
                        auth.signOut()
                    }
            }.addOnFailureListener {
                // Jeśli nie uda się pobrać ID urządzenia, po prostu się wyloguj
                auth.signOut()
            }
        } else {
            // Jeśli z jakiegoś powodu nie ma zalogowanego użytkownika, po prostu wykonaj signOut
            auth.signOut()
        }
    }

    // WAŻNE: Pamiętaj, aby usunąć listenera, gdy ViewModel jest niszczony, aby uniknąć wycieków pamięci.
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        groupsListenerJob?.cancel()
    }
}