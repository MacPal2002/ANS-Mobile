package com.example.test1.ui.schedule


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.data.local.AppDatabase
import com.example.test1.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject


@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleState())
    val uiState: StateFlow<ScheduleState> = _uiState.asStateFlow()

    private val auth = Firebase.auth

    // KROK 1: Dodajemy pole do przechowywania naszego zadania nasłuchującego
    private var groupsListenerJob: Job? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser != null) {
            listenForObservedGroupsChanges()
        } else {
            // KROK 3: Anulujemy nasłuchiwanie PRZED zresetowaniem stanu
            groupsListenerJob?.cancel()
            _uiState.update { ScheduleState() }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    /**
     * Nasłuchuje na zmiany w obserwowanych grupach. Jest to główne źródło danych o grupach.
     * Wywoływana po zalogowaniu i reaguje na każdą zmianę w Firestore.
     */
    private fun listenForObservedGroupsChanges() {
        // Anuluj poprzednie nasłuchiwanie, jeśli jakieś było aktywne
        groupsListenerJob?.cancel()
        // KROK 2: Zapisujemy nowe zadanie nasłuchiwania do naszego pola
        groupsListenerJob = viewModelScope.launch {
            repository.getObservedGroupsFlow().collect { result ->
                result.onSuccess { groups ->
                    val currentSelectedId = _uiState.value.selectedGroupId
                    val newSelectedId = if (groups.any { it.id == currentSelectedId }) {
                        currentSelectedId
                    } else {
                        groups.firstOrNull()?.id
                    }

                    _uiState.update {
                        it.copy(
                            observedGroups = groups,
                            selectedGroupId = newSelectedId
                        )
                    }
                    fetchScheduleForCurrentState()
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            }
        }
    }

    /**
     * Publiczna funkcja wywoływana z UI po zmianie daty.
     */
    fun onDateSelected(newDate: LocalDate) {
        _uiState.update { it.copy(selectedDate = newDate) }
        fetchScheduleForCurrentState()
    }

    /**
     * Publiczna funkcja wywoływana z UI po zmianie grupy w dropdownie.
     */
    fun onGroupSelected(groupId: Int) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
        fetchScheduleForCurrentState()
    }

    /**
     * Centralna funkcja do pobierania planu. Używa aktualnych wartości
     * `selectedGroupId` i `selectedDate` ze stanu.
     */
    private fun fetchScheduleForCurrentState() {
        val state = _uiState.value
        val groupId = state.selectedGroupId

        if (groupId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    events = emptyList(),
                    errorMessage = if (it.observedGroups.isEmpty()) "Nie obserwujesz żadnych grup.\nPrzejdź do ustawień." else "Wybierz grupę."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getDailySchedule(groupId, state.selectedDate)
                .collect { result ->
                    result.onSuccess { scheduleItems ->
                        // Sukces: aktualizujemy plan i czyścimy ewentualny stary błąd
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                events = scheduleItems,
                                errorMessage = null
                            )
                        }
                    }.onFailure { error ->
                        // BŁĄD SIECI: Aktualizujemy TYLKO błąd i stan ładowania.
                        // Lista 'events' pozostaje nietknięta, dzięki czemu dane z cache są nadal widoczne.
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Wystąpił nieznany błąd sieci."
                            )
                        }
                    }
                }
        }
    }

    /**
     * Ważne: Zawsze usuwaj listenery, aby uniknąć wycieków pamięci.
     */
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        groupsListenerJob?.cancel()
    }
}