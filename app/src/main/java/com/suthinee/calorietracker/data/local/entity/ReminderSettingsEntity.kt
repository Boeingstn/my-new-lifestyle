package com.suthinee.calorietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: LocalTime = LocalTime.of(22, 0),
    val quietHoursEnd: LocalTime = LocalTime.of(7, 0)
) {
    companion object {
        const val SINGLETON_ID = 1
        val DEFAULT = ReminderSettingsEntity()
    }
}
