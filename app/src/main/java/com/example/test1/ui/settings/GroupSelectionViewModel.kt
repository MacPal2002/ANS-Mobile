package com.example.test1.ui.settings

import androidx.lifecycle.ViewModel
import com.example.test1.data.GroupNode
import androidx.lifecycle.viewModelScope
import com.example.test1.ui.schedule.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

data class GroupSelectionState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val allGroupsTree: List<GroupNode> = emptyList(),
    val selectedGroupIds: Set<Int> = emptySet()
)

class GroupSelectionViewModel : ViewModel() {
    private val repository = ScheduleRepository()
    private val _uiState = MutableStateFlow(GroupSelectionState())
    val uiState: StateFlow<GroupSelectionState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // 1. Pobierz aktualnie obserwowane grupy
            repository.getObservedGroupIds().onSuccess { ids ->
                _uiState.update { it.copy(selectedGroupIds = ids.toSet()) }
            }.onFailure { /* obsłuż błąd */ }

            // 2. Pobierz całe drzewo grup
            repository.getAllDeanGroups().onSuccess { tree ->
                _uiState.update { it.copy(isLoading = false, allGroupsTree = tree) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = "Błąd: ${error.message}") }
            }
        }
    }

    fun onGroupToggled(groupId: Int, isSelected: Boolean) {
        val currentSelection = _uiState.value.selectedGroupIds.toMutableSet()
        if (isSelected) {
            currentSelection.add(groupId)
        } else {
            currentSelection.remove(groupId)
        }
        _uiState.update { it.copy(selectedGroupIds = currentSelection) }
    }

    fun onSaveSelection() {
        viewModelScope.launch {
            repository.saveObservedGroups(_uiState.value.selectedGroupIds.toList()).onSuccess {
                // Sukces - można nawigować wstecz lub pokazać komunikat
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Błąd zapisu: ${error.message}") }
            }
        }
    }
}