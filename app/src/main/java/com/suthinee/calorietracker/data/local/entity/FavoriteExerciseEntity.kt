package com.suthinee.calorietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_exercises")
data class FavoriteExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityName: String,
    val defaultDuration: Int,
    val estimatedCalories: Int
)
