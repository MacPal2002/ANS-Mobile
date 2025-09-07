package com.example.test1.ui.settings.groupSelection

import androidx.lifecycle.ViewModel
import com.example.test1.data.models.GroupNode
import androidx.lifecycle.viewModelScope
import com.example.test1.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class GroupSelectionState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val allGroupsTree: List<GroupNode> = emptyList(),
    val selectedGroupIds: Set<Int> = emptySet()
)


@HiltViewModel
class GroupSelectionViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSelectionState())
    val uiState: StateFlow<GroupSelectionState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // 1. Pobierz aktualnie obserwowane grupy
            scheduleRepository.getObservedGroupIds().onSuccess { ids ->
                _uiState.update { it.copy(selectedGroupIds = ids.toSet()) }
            }.onFailure { /* obsłuż błąd */ }

            // 2. Pobierz całe drzewo grup
            scheduleRepository.getAllDeanGroups().onSuccess { tree ->
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
            scheduleRepository.saveObservedGroups(_uiState.value.selectedGroupIds.toList()).onSuccess {
                // Sukces - można nawigować wstecz lub pokazać komunikat
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Błąd zapisu: ${error.message}") }
            }
        }
    }
}