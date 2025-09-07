package com.example.test1.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Ten Flow będzie informował, czy użytkownik jest aktualnie zalogowany.
    // Konwertujemy FirebaseUser? na prosty Boolean.
    val isLoggedIn: StateFlow<Boolean> = authRepository.getAuthStateFlow()
        .map { firebaseUser -> firebaseUser != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Utrzymaj Flow aktywny przez 5s po zniknięciu ostatniego subskrybenta
            initialValue = authRepository.getCurrentUserId() != null // Wartość początkowa
        )

    // Prosta właściwość do sprawdzenia stanu początkowego (dla startDestination)
    val isInitiallyLoggedIn: Boolean
        get() = authRepository.getCurrentUserId() != null
}