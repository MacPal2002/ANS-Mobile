package com.example.test1.ui.schedule.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.test1.ui.schedule.DayStartHour
import com.example.test1.ui.schedule.ScheduleEvent
import java.time.LocalTime

@Composable
fun EventGrid(
    events: List<ScheduleEvent>,
    isToday: Boolean,
    currentTime: LocalTime,
    hourHeight: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeight.toPx() }

    val lineColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    // ZMIANA: Dodajemy Box jako główny kontener, aby móc rysować tło i layout
    Box(modifier = modifier) {

        // ZMIANA: Dodajemy Canvas do rysowania linii godzinowych w tle.
        // Te linie będą się przewijać razem z resztą.
        Canvas(modifier = Modifier.matchParentSize()) {
            for (hour in DayStartHour..22) {
                val yPosition = (hour - DayStartHour) * hourHeightPx
                drawLine(
                    color = lineColor,
                    start = Offset(x = 0f, y = yPosition),
                    end = Offset(x = size.width, y = yPosition),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Twój kod z Layout pozostaje w środku, jest teraz rysowany na wierzchu linii
        Layout(
            content = {
                events.forEach { event ->
                    Box(modifier = Modifier.eventData(event)) {
                        EventCard(event)
                    }
                }
                if (isToday) {
                    CurrentTimeLine()
                }
            },
        ) { measurables, constraints ->
            val placeablesWithData = measurables.map { measurable ->
                val eventData = measurable.parentData as? EventData
                val placeable = if (eventData != null) {
                    val event = eventData.event
                    val startOffsetPx = (event.startTime.hour - DayStartHour) * hourHeightPx + (event.startTime.minute / 60f * hourHeightPx)
                    val endOffsetPx = (event.endTime.hour - DayStartHour) * hourHeightPx + (event.endTime.minute / 60f * hourHeightPx)
                    val heightPx = (endOffsetPx - startOffsetPx).coerceAtLeast(0f).toInt()
                    measurable.measure(
                        constraints.copy(minHeight = heightPx, maxHeight = heightPx)
                    )
                } else {
                    measurable.measure(constraints)
                }
                Pair(placeable, eventData)
            }

            layout(constraints.maxWidth, (hourHeightPx * (23 - DayStartHour)).toInt()) {
                placeablesWithData.forEach { (placeable, eventData) ->
                    if (eventData != null) {
                        val event = eventData.event
                        val startOffsetPx = (event.startTime.hour - DayStartHour) * hourHeightPx + (event.startTime.minute / 60f * hourHeightPx)
                        placeable.place(x = 0, y = startOffsetPx.toInt())
                    } else {
                        val offsetPx = (currentTime.hour - DayStartHour) * hourHeightPx + (currentTime.minute / 60f * hourHeightPx)
                        placeable.place(x = 0, y = offsetPx.toInt())
                    }
                }
            }
        }
    }
}

// Customowy modifier do przekazywania danych do layoutu
private class EventData(val event: ScheduleEvent) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@EventData
}
private fun Modifier.eventData(event: ScheduleEvent) = this.then(EventData(event))

