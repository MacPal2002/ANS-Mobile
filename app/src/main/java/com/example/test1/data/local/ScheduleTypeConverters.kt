package com.example.test1.data.local

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class ScheduleTypeConverters {

    private val gson = Gson()

    // Konwertery dla LocalDate
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    // Konwertery dla Timestamp
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): String? {
        return timestamp?.let { "${it.seconds}:${it.nanoseconds}" }
    }

    @TypeConverter
    fun toTimestamp(value: String?): Timestamp? {
        return value?.split(':')?.let { parts ->
            if (parts.size == 2) {
                Timestamp(parts[0].toLong(), parts[1].toInt())
            } else {
                null
            }
        }
    }

    // Konwertery dla List<Map<String, Any>>
    @TypeConverter
    fun fromMapList(list: List<Map<String, Any>>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toMapList(json: String?): List<Map<String, Any>>? {
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return gson.fromJson(json, type)
    }
}