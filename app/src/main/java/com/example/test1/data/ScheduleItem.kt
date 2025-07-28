package com.example.test1.data

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class ScheduleItem(
    val subjectFullName: String = "",
    val classType: String? = null,
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val lecturers: List<Map<String, Any>> = emptyList(),
    val rooms: List<Map<String, Any>> = emptyList()
) {
    // Funkcje pomocnicze do konwersji Timestamp na LocalDateTime
    fun getStartLocalDateTime(): LocalDateTime =
        Instant.ofEpochSecond(startTime.seconds, startTime.nanoseconds.toLong())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

    fun getEndLocalDateTime(): LocalDateTime =
        Instant.ofEpochSecond(endTime.seconds, endTime.nanoseconds.toLong())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
}