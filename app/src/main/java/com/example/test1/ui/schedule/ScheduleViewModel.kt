package com.example.test1.ui.schedule

import com.example.test1.data.repository.AuthRepository
import com.example.test1.data.repository.ScheduleRepository
import com.example.test1.ui.base.AuthenticatedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject


@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    authRepository: AuthRepository
) : AuthenticatedViewModel(authRepository) {

    private val _uiState = MutableStateFlow(ScheduleState())
    val uiState: StateFlow<ScheduleState> = _uiState.asStateFlow()

    private var scheduleFetchJob: Job? = null
    private var groupsListenerJob: Job? = null


    override fun onUserLoggedIn(userId: String) {
        listenForObservedGroupsChanges()
    }

    override fun onUserLoggedOut() {
        super.onUserLoggedOut()
        _uiState.update { ScheduleState() }
    }

    /**
     * Nasłuchuje na zmiany w obserwowanych grupach. Jest to główne źródło danych o grupach.
     * Wywoływana po zalogowaniu i reaguje na każdą zmianę w Firestore.
     */
    private fun listenForObservedGroupsChanges() {
        groupsListenerJob?.cancel()
        groupsListenerJob = launchCancellable {
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
        scheduleFetchJob?.cancel()
        scheduleFetchJob = launchCancellable {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getDailySchedule(groupId, state.selectedDate)
                .collect { result ->
                    result.onSuccess { scheduleItems ->
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

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

