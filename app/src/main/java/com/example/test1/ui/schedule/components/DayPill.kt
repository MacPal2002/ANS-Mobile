package com.example.test1.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayPill(date: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale("pl"))
    val dayOfWeekText = date.format(dayOfWeekFormatter).uppercase()

    val isToday = date == LocalDate.now()
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary     // Ciemne tło dla zaznaczonego dnia
        isToday -> MaterialTheme.colorScheme.tertiaryContainer      // Jasne tło dla dzisiejszego dnia
        else -> Color.Transparent      // Brak tła dla pozostałych
    }

    val dayOfMonthColor = when {
        isSelected -> Color.White      // Biały tekst na ciemnym tle
        isToday -> MaterialTheme.colorScheme.onTertiaryContainer       // Ciemny tekst (główny kolor) na jasnym tle
        else -> MaterialTheme.colorScheme.onBackground              // Domyślny kolor tekstu
    }

    val dayOfWeekColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.onTertiaryContainer
        isWeekend -> Color(0xFFB71C1C)  // Czerwony dla weekendu
        else -> MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .width(56.dp)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = dayOfWeekText, style = MaterialTheme.typography.labelSmall, color = dayOfWeekColor)
        Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, color = dayOfMonthColor)
    }
}