package com.example.test1.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class AuthenticatedViewModel (
    private val authRepository: AuthRepository
) : ViewModel() {

    protected val jobs = mutableListOf<Job>()


    init {
        observeAuthState()
    }

    /**
     * Uruchamia korutynę i automatycznie dodaje ją do listy
     * zadań, które zostaną anulowane w onCleared().
     */
    protected fun launchCancellable(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(block = block).also { jobs.add(it) }
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
     * Ta funkcja ma teraz domyślną implementację.
     * Anuluje wszystkie zadania i czyści listę po wylogowaniu.
     * Jest 'open', więc dzieci mogą dodać swoją logikę (np. reset UI).
     */
    protected open fun onUserLoggedOut() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    /**
     * Anuluje wszystkie aktywne zadania, gdy ViewModel jest niszczony,
     * aby uniknąć wycieków pamięci.
     */
    override fun onCleared() {
        super.onCleared()
        jobs.forEach { it.cancel() }
        jobs.clear()
    }
}