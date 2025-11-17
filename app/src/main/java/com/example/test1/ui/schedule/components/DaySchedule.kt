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

    LaunchedEffect(events) {
        if (events.isNotEmpty()) {
            val firstEvent = events.minByOrNull { it.startTime }
            if (firstEvent != null) {
                val hourOffset = firstEvent.startTime.hour - DayStartHour
                val minuteOffset = firstEvent.startTime.minute / 60f
                val totalOffsetPx = (hourOffset + minuteOffset) * hourHeightPx
                val finalOffset = (totalOffsetPx - (hourHeightPx / 2)).coerceAtLeast(0f)
                scrollState.animateScrollTo(finalOffset.toInt())
            }
        } else {
            scrollState.animateScrollTo(0)
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 30.dp)
    ) {
        TimeGutter(hourHeight = HourHeight)

        EventGrid(
            events = events,
            isToday = isToday,
            currentTime = currentTime,
            hourHeight = HourHeight,
            modifier = Modifier.weight(1f)
        )
    }
}