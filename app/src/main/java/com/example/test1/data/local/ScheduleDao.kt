package com.example.test1.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test1.data.ScheduleItem
import java.time.LocalDate

@Dao
interface ScheduleDao {

    // Pobiera plan dla danej grupy i daty
    @Query("SELECT * FROM schedule_items WHERE groupId = :groupId AND date = :date")
    suspend fun getScheduleByGroupAndDate(groupId: Int, date: LocalDate): List<ScheduleItem>

    // Wstawia listę zajęć. Jeśli już istnieją, zostaną zastąpione
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(items: List<ScheduleItem>)

    // Usuwa stary plan dla danej grupy i daty
    @Query("DELETE FROM schedule_items WHERE groupId = :groupId AND date = :date")
    suspend fun deleteScheduleByGroupAndDate(groupId: Int, date: LocalDate)
}