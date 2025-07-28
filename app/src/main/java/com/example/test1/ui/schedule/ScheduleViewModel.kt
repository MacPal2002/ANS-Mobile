package com.example.test1.ui.schedule


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate


class ScheduleViewModel : ViewModel() {
    private val repository = ScheduleRepository()

    // Prywatny, modyfikowalny stan
    private val _uiState = MutableStateFlow(ScheduleState())
    // Publiczny, niemodyfikowalny stan dla widoku
    val uiState: StateFlow<ScheduleState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Pobierz ID obserwowanych grup
            repository.getObservedGroupIds().onSuccess { ids ->
                if (ids.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Nie obserwujesz żadnych grup.\nPrzejdź do ustawień") }
                    return@launch
                }

                // 2. Pobierz szczegóły (nazwy) tych grup
                repository.getGroupDetails(ids).onSuccess { groups ->
                    val firstGroupId = groups.firstOrNull()?.id
                    _uiState.update { it.copy(observedGroups = groups, selectedGroupId = firstGroupId) }

                    // 3. Pobierz plan dla pierwszej grupy
                    if (firstGroupId != null) {
                        fetchScheduleForDate(firstGroupId, _uiState.value.selectedDate)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd pobierania nazw grup: ${error.message}") }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd pobierania obserwowanych grup: ${error.message}") }
            }
        }
    }

    fun onDateSelected(newDate: LocalDate) {
        _uiState.update { it.copy(selectedDate = newDate) }
        val groupId = _uiState.value.selectedGroupId
        if (groupId != null) {
            fetchScheduleForDate(groupId, newDate)
        }
    }

    // Obsługuje wybór innej grupy z listy
    fun onGroupSelected(groupId: Int) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
        fetchScheduleForDate(groupId, _uiState.value.selectedDate)
    }

    private fun fetchScheduleForDate(groupId: Int, date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getDailySchedule(groupId, date)

            result.onSuccess { newEvents ->
                _uiState.update { it.copy(isLoading = false, events = newEvents) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "Błąd: ${error.message}") }
            }
        }
    }
}