package com.example.test1.ui.schedule

import com.example.test1.data.models.ObservedGroup
import com.example.test1.data.local.ScheduleItem
import java.time.LocalDate



data class ScheduleState(
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<ScheduleItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val observedGroups: List<ObservedGroup> = emptyList(),
    val selectedGroupId: Int? = null
)