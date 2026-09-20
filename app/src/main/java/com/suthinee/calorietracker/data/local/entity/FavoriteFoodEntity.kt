package com.suthinee.calorietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suthinee.calorietracker.domain.model.MealType

@Entity(tableName = "favorite_foods")
data class FavoriteFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodName: String,
    val defaultPortion: String,
    val calories: Int,
    val mealType: MealType? = null
)
