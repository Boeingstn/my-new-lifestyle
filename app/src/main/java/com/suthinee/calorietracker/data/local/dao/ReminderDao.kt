package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suthinee.calorietracker.data.local.entity.ReminderEntity
import com.suthinee.calorietracker.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY type ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE type = :type LIMIT 1")
    suspend fun getByType(type: ReminderType): ReminderEntity?

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun count(): Int
}
