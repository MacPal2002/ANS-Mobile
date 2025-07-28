package com.example.test1.ui.login

// Definicja możliwych stanów ekranu
enum class LoginStatus {
    IDLE,       // Oczekiwanie
    LOADING,    // Ładowanie
    SUCCESS,    // Sukces
    ERROR       // Błąd
}

data class LoginState(
    val albumNumber: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.IDLE,

    val albumNumberError: String? = null,
    val passwordError: String? = null,
    val genericError: String? = null
)