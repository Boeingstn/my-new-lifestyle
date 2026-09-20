package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.suthinee.calorietracker.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface FoodLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FoodLogEntity): Long

    @Update
    suspend fun update(entity: FoodLogEntity)

    @Delete
    suspend fun delete(entity: FoodLogEntity)

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE date BETWEEN :start AND :end ORDER BY date ASC, createdAt ASC")
    fun observeForRange(start: LocalDate, end: LocalDate): Flow<List<FoodLogEntity>>

    @Query("SELECT COALESCE(SUM(calories), 0) FROM food_logs WHERE date = :date")
    fun observeTotalCaloriesForDate(date: LocalDate): Flow<Int>
}
