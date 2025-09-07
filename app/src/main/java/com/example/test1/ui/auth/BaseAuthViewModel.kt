package com.example.test1.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Definiujemy joby, które mogą być używane przez dziedziczące ViewModel'e
    protected var dataLoadingJob: Job? = null
    protected var groupsListenerJob: Job? = null

    init {
        observeAuthState()
    }

    /**
     * Centralna funkcja obserwująca stan logowania.
     * Wywołuje abstrakcyjne metody w zależności od tego, czy użytkownik jest zalogowany.
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                if (user != null) {
                    onUserLoggedIn(user.uid)
                } else {
                    onUserLoggedOut()
                }
            }
        }
    }

    /**
     * Ta funkcja musi zostać zaimplementowana przez ViewModel-dziecko.
     * Określa, co ma się stać po pomyślnym zalogowaniu.
     * @param userId ID zalogowanego użytkownika.
     */
    protected abstract fun onUserLoggedIn(userId: String)

    /**
     * Ta funkcja musi zostać zaimplementowana przez ViewModel-dziecko.
     * Określa, co ma się stać po wylogowaniu.
     */
    protected abstract fun onUserLoggedOut()

    /**
     * Anuluje wszystkie aktywne zadania, gdy ViewModel jest niszczony,
     * aby uniknąć wycieków pamięci.
     */
    override fun onCleared() {
        super.onCleared()
        dataLoadingJob?.cancel()
        groupsListenerJob?.cancel()
    }
}