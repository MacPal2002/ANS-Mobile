package com.example.test1.ui.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarGrid(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = displayedMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val daysInMonth = displayedMonth.lengthOfMonth()

    val emptyCells = firstDayOfWeek - 1

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val allCells = (1..emptyCells).map { null } + (1..daysInMonth).map { displayedMonth.atDay(it) }
        val chunks = allCells.chunked(7) // Dzielimy na tygodnie

        chunks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date != null) {
                            CalendarDay(
                                date = date,
                                isSelected = date == selectedDate,
                                onDateSelected = { onDateSelected(it) }
                            )
                        }
                    }
                }
                if (week.size < 7) {
                    for (i in 1..(7 - week.size)) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}