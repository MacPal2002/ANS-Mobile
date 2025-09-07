package com.example.test1.ui.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.example.test1.data.repository.AuthRepository
import com.example.test1.data.repository.ScheduleRepository
import com.example.test1.data.repository.SettingsRepository
import com.example.test1.ui.auth.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository
) : BaseAuthViewModel(authRepository) {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()


    init {
        collectTheme()
    }

    override fun onUserLoggedIn(userId: String) {
        loadInitialUserData(userId)
    }

    override fun onUserLoggedOut() {
        dataLoadingJob?.cancel()
        groupsListenerJob?.cancel()
        _uiState.update { currentState ->
            SettingsState(themeOption = currentState.themeOption)
        }
    }

    private fun loadInitialUserData(userId: String) {
        dataLoadingJob?.cancel()
        dataLoadingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = settingsRepository.getUserData(userId)

            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _uiState.update {
                    it.copy(
                        displayName = user.displayName,
                        albumNumber = user.albumNumber,
                        notificationsEnabled = user.notificationsEnabled,
                        notificationTimeOption = user.notificationTimeOption,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = result.exceptionOrNull()?.message,
                        isLoading = false
                    )
                }
            }
            listenForObservedGroupsChanges()
        }
    }


    private fun listenForObservedGroupsChanges() {
        groupsListenerJob?.cancel()
        groupsListenerJob = viewModelScope.launch {
            scheduleRepository.getObservedGroupsFlow().collect { result ->
                result.onSuccess { groups ->
                    _uiState.update {
                        it.copy(
                            observedGroups = groups.map { g -> g.name }.ifEmpty { listOf("Brak") }
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            }
        }
    }

    private fun collectTheme() {
        viewModelScope.launch {
            settingsRepository.themeOptionFlow.collect { theme ->
                _uiState.update { it.copy(themeOption = theme) }
            }
        }
    }

    fun onThemeChange(theme: String) {
        viewModelScope.launch {
            settingsRepository.saveThemeOption(theme)
        }
    }

    fun onNotificationTimeChange(time: String) {
        _uiState.update { it.copy(notificationTimeOption = time) }
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            settingsRepository.updateDeviceSetting(userId, "notificationTimeOption", time)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            settingsRepository.updateDeviceSetting(userId, "notificationEnabled", enabled)
        }
    }

    fun onNotificationToggleRequested(
        isChecked: Boolean,
        requestPermission: () -> Unit,
    ) {
        val userId = authRepository.getCurrentUserId() ?: return
        if (isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission()
            } else {
                viewModelScope.launch {
                    settingsRepository.updateDeviceSetting(userId, "notificationEnabled", true)
                }
                _uiState.update { it.copy(notificationsEnabled = true) }
            }
        } else {
            viewModelScope.launch {
                settingsRepository.updateDeviceSetting(userId, "notificationEnabled", false)
            }
            _uiState.update { it.copy(notificationsEnabled = false) }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
