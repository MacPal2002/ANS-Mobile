package com.example.test1.ui.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test1.ui.schedule.ScheduleEvent
import java.time.format.DateTimeFormatter

@Composable
fun EventCard(event: ScheduleEvent) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeRange = "${event.startTime.toLocalTime().format(timeFormatter)} - ${event.endTime.toLocalTime().format(timeFormatter)}"

    Card(
        modifier = Modifier
            .fillMaxSize()
            // ZMIANA 3: Dodajemy padding na dole, aby stworzyć odstęp
            .padding(end = 16.dp, start = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(12.dp), // Ładniejsze, bardziej zaokrąglone rogi
        colors = CardDefaults.cardColors(containerColor = event.color)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Główna treść, wyrównana do góry
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = event.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                event.details.forEach { detail ->
                    Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Godzina, "przyklejona" do prawego dolnego rogu
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Czas trwania",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = timeRange, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        }
    }
}