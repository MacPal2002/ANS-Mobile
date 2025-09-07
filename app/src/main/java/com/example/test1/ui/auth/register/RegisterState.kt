package com.example.test1.ui.auth.register


enum class RegisterStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

data class RegisterState(
    val currentStep: Int = 1,
    val albumNumber: String = "",
    val albumNumberError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val verbisPassword: String = "",
    val status: RegisterStatus = RegisterStatus.IDLE,
    val error: String? = null,
)