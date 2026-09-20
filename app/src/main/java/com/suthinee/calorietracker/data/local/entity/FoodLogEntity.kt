package com.suthinee.calorietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suthinee.calorietracker.domain.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val foodName: String,
    val portion: String,
    val calories: Int,
    val photoPath: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
