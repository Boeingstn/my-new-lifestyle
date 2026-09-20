package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suthinee.calorietracker.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WaterLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WaterLogEntity): Long

    @Delete
    suspend fun delete(entity: WaterLogEntity)

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<WaterLogEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE date = :date")
    fun observeTotalForDate(date: LocalDate): Flow<Int>

    @Query("SELECT date, COALESCE(SUM(amountMl), 0) as total FROM water_logs WHERE date BETWEEN :start AND :end GROUP BY date")
    fun observeDailyTotalsForRange(start: LocalDate, end: LocalDate): Flow<List<DailyTotal>>
}

data class DailyTotal(
    val date: LocalDate,
    val total: Int
)
