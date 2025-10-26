package com.example.test1.ui.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun ScrollableWeekRow(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp, horizontal = 16.dp),

        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dates.forEach { date ->
            DayPill(
                date = date,
                isSelected = (date == selectedDate),
                onClick = { onDateSelected(date) }
            )
        }
    }
}