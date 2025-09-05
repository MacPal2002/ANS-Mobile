package com.example.test1.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val installations: FirebaseInstallations,
    private val firestore: FirebaseFirestore,
    ) : ViewModel() {


    // Prywatny, modyfikowalny stan
    private val _uiState = MutableStateFlow(LoginState())
    // Publiczny, niemodyfikowalny stan, którego nasłuchuje UI
    val uiState = _uiState.asStateFlow()

    // Funkcja wywoływana przez UI, gdy zmieni się tekst w polu email
    fun onAlbumNumberChange(albumNumber: String) {
        _uiState.update { it.copy(albumNumber = albumNumber, albumNumberError = null, genericError = null, status = LoginStatus.IDLE) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, genericError = null, status = LoginStatus.IDLE) }
    }


    private suspend fun saveDeviceData() {
        try {
            val userId = auth.currentUser?.uid ?: return

            val deviceId = installations.id.await()
            val token = Firebase.messaging.token.await()

            val deviceUpdates = mapOf(
                "devices.$deviceId.token" to token,
                "devices.$deviceId.notificationEnabled" to false,
                "devices.$deviceId.notificationTimeOption" to "30 minut",
                "lastLogin" to FieldValue.serverTimestamp()
            )

            firestore.collection("students").document(userId)
                .update(deviceUpdates)
                .await()

            Log.d("Firestore", "Device data and lastLogin updated for device: $deviceId")

        } catch (e: Exception) {
            Log.w("Firestore", "Error updating device data", e)
        }
    }

    // Główna funkcja logiki logowania
    fun login() {
        // 1. Czyszczenie starych błędów i walidacja pól
        _uiState.update { it.copy(albumNumberError = null, passwordError = null, genericError = null) }
        val albumNumber = _uiState.value.albumNumber
        val password = _uiState.value.password
        var hasError = false

        if (albumNumber.isBlank()) {
            _uiState.update { it.copy(albumNumberError = "Pole 'numer albumu' nie może być puste") }
            hasError = true
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Pole 'hasło' nie może być puste") }
            hasError = true
        }
        if (hasError) return

        // 2. Rozpoczęcie procesu logowania (etap 1: zapytanie do Firestore)
        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.LOADING) }
            try {
                val document = firestore.collection("student_lookups")
                    .document(albumNumber)
                    .get()
                    .await()

                    if (document != null && document.exists()) {
                        // SUKCES: Znaleziono dokument, pobieramy z niego email
                        val email = document.getString("email")
                        if (email != null) {
                            // Mamy email, przechodzimy do etapu 2: logowanie w Firebase Auth
                            signInToFirebaseAuth(email, password)
                        } else {
                            // Sytuacja awaryjna: znaleziono studenta, ale nie ma zapisanego emaila
                            _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Błąd danych użytkownika. Skontaktuj się z administratorem.") }
                        }
                    } else {
                        // BŁĄD: Nie znaleziono studenta o takim numerze
                        _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Student o podanym numerze albumu nie istnieje.") }
                    }
            } catch (e: Exception) {
            // Jeden blok catch łapie błędy z get() i signInWithEmailAndPassword()
            _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Błąd: ${e.message}") }
            }

        }
    }

    private suspend fun signInToFirebaseAuth(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            saveDeviceData()
            _uiState.update { it.copy(status = LoginStatus.SUCCESS) }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Nieprawidłowe hasło.") }
        } catch (e: Exception) {
            // Inne błędy
            _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Wystąpił nieoczekiwany błąd.") }
        }
    }
}
