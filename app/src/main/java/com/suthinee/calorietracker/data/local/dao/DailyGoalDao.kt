package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suthinee.calorietracker.data.local.entity.DailyGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyGoalEntity)

    @Query("SELECT * FROM daily_goal WHERE id = ${DailyGoalEntity.SINGLETON_ID}")
    fun observe(): Flow<DailyGoalEntity?>
}
