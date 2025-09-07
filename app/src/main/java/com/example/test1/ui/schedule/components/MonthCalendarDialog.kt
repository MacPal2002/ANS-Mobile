package com.example.test1.ui.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthCalendarDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    val daysOfWeek = listOf("PN", "WT", "ŚR", "CZW", "PT", "SO", "ND")

    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.width(320.dp).height(440.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Nagłówek z nazwą miesiąca i nawigacją
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, "Poprzedni miesiąc")
                    }
                    Text(
                        text = "${
                            displayedMonth.month.getDisplayName(TextStyle.FULL, Locale("pl"))
                                .replaceFirstChar { it.titlecase() }
                        } ${displayedMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, "Następny miesiąc")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Nagłówki dni tygodnia
                Row(modifier = Modifier.fillMaxWidth()) {
                    daysOfWeek.forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Siatka z dniami
                CalendarGrid(
                    displayedMonth = displayedMonth,
                    selectedDate = initialDate,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}