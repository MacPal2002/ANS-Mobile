package com.example.test1.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateHeader(
    selectedDate: LocalDate,
    onTodayClick: () -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val currentYear = LocalDate.now().year
    val pattern = if (selectedDate.year == currentYear) {
        "E, d MMMM" // Format bez roku, np. "Wt., 24 czerwca"
    } else {
        "E, d MMMM uuuu" // Pełny format z rokiem, np. "Śr., 25 czerwca 2026"
    }
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale("pl"))
    val isToday = selectedDate == LocalDate.now()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(90.dp),
            contentAlignment = Alignment.Center
        ){
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable(onClick = onDateClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "Otwórz kalendarz",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Tytuł z datą
        Text(
            text = selectedDate.format(formatter).replaceFirstChar { it.titlecase(Locale("pl")) },
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Kontener na przycisk po prawej stronie
        Box(
            modifier = Modifier.width(90.dp),
            contentAlignment = Alignment.Center
        ) {

            if (!isToday) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable(onClick = onTodayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Wróć do dzisiaj",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}