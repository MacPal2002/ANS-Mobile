package com.example.test1.ui.schedule

import com.example.test1.data.ScheduleItem
import java.time.LocalDate

data class ObservedGroup(val id: Int, val name: String)

data class ScheduleState(
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<ScheduleItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val observedGroups: List<ObservedGroup> = emptyList(),
    val selectedGroupId: Int? = null
)