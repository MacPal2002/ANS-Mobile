package com.example.test1.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.ui.schedule.ScheduleRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val scheduleRepository = ScheduleRepository()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // --- Krok 1: Pobierz dane użytkownika z dokumentu "students" ---
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "Użytkownik nie jest zalogowany.") }
                return@launch
            }

            try {
                val userDoc = db.collection("students").document(currentUser.uid).get().await()
                if (userDoc.exists()) {
                    // Pobierz podstawowe dane
                    _uiState.update {
                        it.copy(
                            displayName = userDoc.getString("displayName") ?: "Brak nazwy",
                            albumNumber = userDoc.getString("albumNumber") ?: "Brak albumu",
                            notificationTimeOption = userDoc.getString("notificationTimeOption") ?: "30 minut",
                            notificationsEnabled = userDoc.getBoolean("notificationsEnabled") ?: true
                        )
                    }

                    // Pobierz ID obserwowanych grup
                    val rawData = userDoc.get("observedGroups")
                    val intGroupIds = (rawData as? List<*>)
                        ?.mapNotNull { (it as? Long)?.toInt() }
                        ?: emptyList()

                    // --- Krok 2: Pobierz nazwy obserwowanych grup ---
                    if (intGroupIds.isNotEmpty()) {
                        scheduleRepository.getGroupDetails(intGroupIds).onSuccess { groups ->
                            _uiState.update { it.copy(observedGroups = groups.map { g -> g.name }) }
                        }.onFailure { error ->
                            _uiState.update { it.copy(error = "Błąd pobierania nazw grup: ${error.message}") }
                        }
                    } else {
                        _uiState.update { it.copy(observedGroups = listOf("Brak")) }
                    }

                } else {
                    _uiState.update { it.copy(error = "Nie znaleziono danych studenta.") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd: ${e.message}") }
            }

            _uiState.update { it.copy(isLoading = false) }
        }

        // --- Krok 3: Wczytaj motyw z DataStore ---
        viewModelScope.launch {
            settingsRepository.themeOptionFlow.collect { theme ->
                _uiState.update { it.copy(themeOption = theme) }
            }
        }
    }

    private fun updateFirestoreSetting(key: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("students").document(userId)
            .update(key, value)
            .addOnFailureListener { e ->
                _uiState.update { it.copy(error = "Błąd zapisu ustawień: ${e.message}") }
            }
    }

    fun onThemeChange(theme: String) {
        viewModelScope.launch {
            settingsRepository.saveThemeOption(theme)
        }
    }

    fun onNotificationTimeChange(time: String) {
        _uiState.update { it.copy(notificationTimeOption = time) }
        updateFirestoreSetting("notificationTimeOption", time)
    }

    fun onNotificationsToggle(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        updateFirestoreSetting("notificationsEnabled", enabled)
    }

    fun onLogout() {
        auth.signOut()
    }
}