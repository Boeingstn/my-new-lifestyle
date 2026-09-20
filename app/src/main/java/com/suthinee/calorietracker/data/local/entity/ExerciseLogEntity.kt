package com.suthinee.calorietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "exercise_logs")
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val activityName: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
