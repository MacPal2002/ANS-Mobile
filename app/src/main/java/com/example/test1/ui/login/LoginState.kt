package com.example.test1.ui.login

data class LoginState(
    val studentId: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,

    val studentIdError: String? = null,
    val passwordError: String? = null,
    val genericError: String? = null
)