package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteExerciseEntity): Long

    @Update
    suspend fun update(entity: FavoriteExerciseEntity)

    @Delete
    suspend fun delete(entity: FavoriteExerciseEntity)

    @Query("SELECT * FROM favorite_exercises ORDER BY activityName ASC")
    fun observeAll(): Flow<List<FavoriteExerciseEntity>>
}
