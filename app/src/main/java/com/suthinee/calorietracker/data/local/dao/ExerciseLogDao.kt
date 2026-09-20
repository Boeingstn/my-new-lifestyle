package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suthinee.calorietracker.data.local.entity.ExerciseLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExerciseLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExerciseLogEntity): Long

    @Delete
    suspend fun delete(entity: ExerciseLogEntity)

    @Query("SELECT * FROM exercise_logs WHERE date = :date ORDER BY createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_logs WHERE date BETWEEN :start AND :end ORDER BY date ASC, createdAt ASC")
    fun observeForRange(start: LocalDate, end: LocalDate): Flow<List<ExerciseLogEntity>>

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM exercise_logs WHERE date = :date")
    fun observeTotalBurnedForDate(date: LocalDate): Flow<Int>
}
