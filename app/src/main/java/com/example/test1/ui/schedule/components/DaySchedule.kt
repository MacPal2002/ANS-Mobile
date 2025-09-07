package com.example.test1.ui.schedule.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.test1.ui.schedule.DayStartHour
import com.example.test1.ui.schedule.HourHeight
import com.example.test1.ui.schedule.ScheduleEvent
import java.time.LocalTime

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DaySchedule(
    events: List<ScheduleEvent>,
    isToday: Boolean,
    currentTime: LocalTime,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val density = LocalDensity.current
    val hourHeightPx = remember(density) {
        with(density) { HourHeight.toPx() }
    }

    // Efekt będzie uruchamiany za każdym razem, gdy zmieni się lista wydarzeń (czyli dzień)
    LaunchedEffect(events) {
        if (isToday) {
            // Dla dzisiejszego dnia: przewiń do aktualnej godziny
            val hourOffset = currentTime.hour - DayStartHour
            val minuteOffset = currentTime.minute / 60f
            val totalOffsetPx = (hourOffset + minuteOffset) * hourHeightPx
            scrollState.animateScrollTo(totalOffsetPx.toInt().coerceAtLeast(0))
        } else {
            // ✅ NOWA LOGIKA: Dla innych dni
            if (events.isNotEmpty()) {
                // Jeśli są zajęcia, znajdź pierwsze z nich
                val firstEvent = events.minByOrNull { it.startTime }
                if (firstEvent != null) {
                    // Oblicz pozycję pierwszych zajęć i przewiń do nich
                    val hourOffset = firstEvent.startTime.hour - DayStartHour
                    val minuteOffset = firstEvent.startTime.minute / 60f
                    val totalOffsetPx = (hourOffset + minuteOffset) * hourHeightPx
                    // Przewijamy odrobinę wyżej, aby był margines
                    val finalOffset = (totalOffsetPx - (hourHeightPx / 2)).coerceAtLeast(0f)
                    scrollState.animateScrollTo(finalOffset.toInt())
                }
            } else {
                // Jeśli nie ma zajęć, przewiń na samą górę
                scrollState.animateScrollTo(0)
            }
        }
    }

    // Tło z liniami godzinowymi
    Row(
        modifier = modifier
            .fillMaxSize() // Dodajemy fillMaxSize, aby zająć dostępną przestrzeň
            .verticalScroll(scrollState)
            .padding(top = 30.dp)
    ) {
        // Kolumna z godzinami po lewej
        TimeGutter(hourHeight = HourHeight)

        // Siatka z wydarzeniami, która teraz sama rysuje swoje tło
        EventGrid(
            events = events,
            isToday = isToday,
            currentTime = currentTime,
            hourHeight = HourHeight,
            modifier = Modifier.weight(1f)
        )
    }
}