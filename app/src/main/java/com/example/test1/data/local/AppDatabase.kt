package com.example.test1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ScheduleItem::class], version = 1, exportSchema = false)
@TypeConverters(ScheduleTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleItemDao
}
