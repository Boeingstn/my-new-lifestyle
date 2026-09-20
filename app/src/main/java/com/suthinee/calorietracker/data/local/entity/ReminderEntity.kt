package com.suthinee.calorietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suthinee.calorietracker.domain.model.ReminderType
import java.time.LocalTime

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val type: ReminderType,
    val enabled: Boolean,
    val time: LocalTime
) {
    companion object {
        fun defaults(): List<ReminderEntity> = listOf(
            ReminderEntity(ReminderType.BREAKFAST, enabled = false, time = LocalTime.of(8, 0)),
            ReminderEntity(ReminderType.LUNCH, enabled = false, time = LocalTime.of(12, 0)),
            ReminderEntity(ReminderType.DINNER, enabled = false, time = LocalTime.of(18, 30)),
            ReminderEntity(ReminderType.SNACK, enabled = false, time = LocalTime.of(15, 0)),
            ReminderEntity(ReminderType.WATER, enabled = false, time = LocalTime.of(10, 0))
        )
    }
}
