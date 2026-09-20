package com.suthinee.calorietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goal")
data class DailyGoalEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val calorieGoal: Int,
    val waterGoal: Int
) {
    companion object {
        const val SINGLETON_ID = 1
        val DEFAULT = DailyGoalEntity(calorieGoal = 2000, waterGoal = 2000)
    }
}
