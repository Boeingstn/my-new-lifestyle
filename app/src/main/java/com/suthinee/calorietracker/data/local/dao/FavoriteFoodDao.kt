package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteFoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteFoodEntity): Long

    @Update
    suspend fun update(entity: FavoriteFoodEntity)

    @Delete
    suspend fun delete(entity: FavoriteFoodEntity)

    @Query("SELECT * FROM favorite_foods ORDER BY foodName ASC")
    fun observeAll(): Flow<List<FavoriteFoodEntity>>
}
