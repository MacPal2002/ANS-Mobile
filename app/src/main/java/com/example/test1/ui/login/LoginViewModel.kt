package com.example.test1.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    // Prywatny, modyfikowalny stan
    private val _uiState = MutableStateFlow(LoginState())
    // Publiczny, niemodyfikowalny stan, którego nasłuchuje UI
    val uiState = _uiState.asStateFlow()

    // Funkcja wywoływana przez UI, gdy zmieni się tekst w polu email
    fun onStudentIdChange(studentId: String) {
        _uiState.update { it.copy(studentId = studentId, studentIdError = null, genericError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, genericError = null) }
    }

    // Główna funkcja logiki logowania
    fun login() {
        // 1. Czyszczenie starych błędów i walidacja pól
        _uiState.update { it.copy(studentIdError = null, passwordError = null, genericError = null) }
        val studentId = _uiState.value.studentId
        val password = _uiState.value.password
        var hasError = false

        if (studentId.isBlank()) {
            _uiState.update { it.copy(studentIdError = "Pole 'numer albumu' nie może być puste") }
            hasError = true
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Pole 'hasło' nie może być puste") }
            hasError = true
        }
        if (hasError) return

        // 2. Rozpoczęcie procesu logowania (etap 1: zapytanie do Firestore)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            db.collection("students")
                .whereEqualTo("studentId", studentId)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Nie znaleziono studenta o takim numerze
                        _uiState.update { it.copy(isLoading = false, genericError = "Student o podanym numerze albumu nie istnieje.") }
                    } else {
                        // Znaleziono dokument, pobieramy z niego email
                        val email = documents.first().getString("email")
                        if (email != null) {
                            // Mamy email, przechodzimy do etapu 2: logowanie w Firebase Auth
                            signInToFirebaseAuth(email, password)
                        } else {
                            // Sytuacja awaryjna: znaleziono studenta, ale nie ma zapisanego emaila
                            _uiState.update { it.copy(isLoading = false, genericError = "Błąd danych użytkownika. Skontaktuj się z administratorem.") }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Błąd połączenia z bazą danych
                    _uiState.update { it.copy(isLoading = false, genericError = "Błąd połączenia z bazą danych: ${exception.message}") }
                }
        }
    }

    // NOWOŚĆ: Prywatna funkcja pomocnicza do logowania w Auth (etap 2)
    private fun signInToFirebaseAuth(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Pobieramy UID zalogowanego użytkownika
                    val userId = task.result.user?.uid
                    if (userId != null) {
                        // Tworzymy referencję do jego dokumentu w Firestore
                        db.collection("students").document(userId)
                            // Używamy update(), aby zaktualizować tylko jedno pole, a nie nadpisać cały dokument
                            .update("lastLogin", FieldValue.serverTimestamp())
                            .addOnSuccessListener {
                                // Opcjonalnie: logowanie sukcesu aktualizacji, przydatne do debugowania
                                Log.d("Firestore", "lastLogin timestamp updated successfully.")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating lastLogin timestamp", e)
                            }
                    }
                    // Aktualizujemy stan UI, aby przejść dalej.
                    // Robimy to od razu, nie czekając na wynik aktualizacji w Firestore.
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }

                } else {
                    // Błąd logowania w Auth - na tym etapie niemal na pewno oznacza to złe hasło.
                    _uiState.update { it.copy(isLoading = false, genericError = "Nieprawidłowe hasło.") }
                }
            }
    }
}
