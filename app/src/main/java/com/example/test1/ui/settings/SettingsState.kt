package com.example.test1.ui.settings

data class SettingsState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val displayName: String = "Ładowanie...",
    val albumNumber: String = "",
    val deanGroupName: String = "",
    val observedGroups: List<String> = emptyList(),
    val themeOption: String = "Systemowy",
    val notificationTimeOption: String = "15 minut",
    val notificationsEnabled: Boolean = false,
)