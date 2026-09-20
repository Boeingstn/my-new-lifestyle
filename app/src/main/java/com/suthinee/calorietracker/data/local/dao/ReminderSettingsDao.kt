package com.suthinee.calorietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suthinee.calorietracker.data.local.entity.ReminderSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderSettingsEntity)

    @Query("SELECT * FROM reminder_settings WHERE id = ${ReminderSettingsEntity.SINGLETON_ID}")
    fun observe(): Flow<ReminderSettingsEntity?>
}
