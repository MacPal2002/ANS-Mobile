package com.example.test1.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
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

    fun login() {
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

        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.LOADING) }
            authRepository.getEmailForAlbumNumber(albumNumber).onSuccess { email ->
                authRepository.signInWithEmail(email, password).onSuccess {
                    _uiState.update { it.copy(status = LoginStatus.SUCCESS) }
                }.onFailure { exception ->
                    handleAuthError(exception)
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = exception.message) }
            }

        }
    }
    private fun handleAuthError(exception: Throwable) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Nieprawidłowe hasło."
            else -> "Wystąpił nieoczekiwany błąd."
        }
        _uiState.update { it.copy(status = LoginStatus.ERROR, genericError = errorMessage) }
    }
}
