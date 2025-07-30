package com.example.test1.ui.login

import android.content.Context
import com.google.firebase.installations.ktx.installations
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.messaging.ktx.messaging

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

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


    private fun saveDeviceData(context: Context) {
        val userId = auth.currentUser?.uid ?: return

        // ZMIANA: Użyj Firebase Installation ID zamiast ANDROID_ID
        Firebase.installations.id.addOnSuccessListener { deviceId ->
            Firebase.messaging.token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result

                val deviceUpdates = mapOf(
                    "devices.$deviceId.token" to token,
                    "devices.$deviceId.notificationEnabled" to false, // Domyślnie wyłączone
                    "devices.$deviceId.notificationTimeOption" to "30 minut", // Domyślnie 30 minut
                    "lastLogin" to FieldValue.serverTimestamp()
                )

                db.collection("students").document(userId)
                    .update(deviceUpdates)
                    .addOnSuccessListener { Log.d("Firestore", "Device data and lastLogin updated for device: $deviceId") }
                    .addOnFailureListener { e -> Log.w("Firestore", "Error updating device data", e) }
            }
        }
    }

    // Główna funkcja logiki logowania
    fun login(context: Context) {
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

            db.collection("student_lookups")
                .document(albumNumber)
                .get()
                .addOnSuccessListener { document ->
                    // POPRAWIONA LOGIKA: Sprawdzamy, czy dokument istnieje
                    if (document != null && document.exists()) {
                        // SUKCES: Znaleziono dokument, pobieramy z niego email
                        val email = document.getString("email")
                        if (email != null) {
                            // Mamy email, przechodzimy do etapu 2: logowanie w Firebase Auth
                            signInToFirebaseAuth(email, password, context)
                        } else {
                            // Sytuacja awaryjna: znaleziono studenta, ale nie ma zapisanego emaila
                            _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Błąd danych użytkownika. Skontaktuj się z administratorem.") }
                        }
                    } else {
                        // BŁĄD: Nie znaleziono studenta o takim numerze
                        _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Student o podanym numerze albumu nie istnieje.") }
                    }
                }
                .addOnFailureListener { exception ->
                    // Błąd połączenia z bazą danych
                    _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Błąd połączenia z bazą danych: ${exception.message}") }
                }
        }
    }

    private fun signInToFirebaseAuth(email: String, password: String, context: Context) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                saveDeviceData(context)

                // Aktualizujemy stan UI, aby przejść dalej
                _uiState.update { it.copy(status = LoginStatus.SUCCESS) }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = "Nieprawidłowe hasło.") }
            }
    }
}
