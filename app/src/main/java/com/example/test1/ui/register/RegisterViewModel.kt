package com.example.test1.ui.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val functions: FirebaseFunctions,

) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    // --- Event Handlers ---
    fun onAlbumNumberChange(value: String) {
        _uiState.update { it.copy(albumNumber = value) }
    }
    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }
    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }
    fun onVerbisPasswordChange(value: String) {
        _uiState.update { it.copy(verbisPassword = value) }
    }
    fun onNextStep() {
        // Pobieramy aktualny stan
        val currentState = _uiState.value
        var hasError = false

        // Walidacja numeru albumu
        if (currentState.albumNumber.isBlank()) {
            _uiState.update { it.copy(albumNumberError = "Pole nie może być puste") }
            hasError = true
        }

        // Walidacja e-maila
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Pole nie może być puste") }
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.update { it.copy(emailError = "Nieprawidłowy format adresu e-mail") }
            hasError = true
        }

        // Walidacja haseł
        if (currentState.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Hasło musi mieć co najmniej 6 znaków") }
            hasError = true
        } else if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Hasła nie są zgodne") }
            hasError = true
        }

        // Jeśli wystąpił jakikolwiek błąd, przerywamy funkcję
        if (hasError) return

        // Jeśli wszystko jest w porządku, przechodzimy dalej
        _uiState.update { it.copy(currentStep = 2) }
    }
    fun onPreviousStep() {
        _uiState.update { it.copy(currentStep = 1) }
    }

    fun onRegisterClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = RegisterStatus.LOADING) }

            val currentState = _uiState.value
            val data = hashMapOf(
                "email" to currentState.email,
                "password" to currentState.password,
                "albumNumber" to currentState.albumNumber,
                "verbisPassword" to currentState.verbisPassword
            )

            try {
                functions
                    .getHttpsCallable("registerStudent")
                    .call(data)
                    .await()

                _uiState.update { it.copy(status = RegisterStatus.SUCCESS) }
            } catch (e: Exception) {
                _uiState.update { it.copy(status = RegisterStatus.ERROR, error = e.message) }
            }
        }
    }

    fun onErrorMessageShown() {
        _uiState.update { it.copy(error = null, status = RegisterStatus.IDLE) }
    }
}