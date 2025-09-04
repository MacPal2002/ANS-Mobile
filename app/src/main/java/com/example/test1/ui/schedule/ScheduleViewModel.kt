package com.example.test1.ui.schedule


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job



class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ScheduleState())
    val uiState: StateFlow<ScheduleState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = AppDatabase.getInstance(application)
    private val scheduleDao = db.scheduleDao()
    private val repository = ScheduleRepository(scheduleDao = scheduleDao)

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
            // Obsługa sytuacji, gdy nie ma wybranej żadnej grupy (np. użytkownik nie obserwuje żadnej)
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
            // Zakładam, że Twoje repozytorium ma taką funkcję
            val result = repository.getDailySchedule(groupId, state.selectedDate)
            result.onSuccess { newEvents ->
                _uiState.update { it.copy(isLoading = false, events = newEvents) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd: ${error.message}") }
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