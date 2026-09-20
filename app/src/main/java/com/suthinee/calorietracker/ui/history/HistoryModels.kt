package com.suthinee.calorietracker.ui.history

import java.time.LocalDate

data class DaySummary(
    val date: LocalDate,
    val caloriesConsumed: Int,
    val waterMl: Int,
    val exerciseSessions: Int,
    val caloriesBurned: Int
)

data class WeeklyStats(
    val days: List<DaySummary>,
    val calorieGoal: Int,
    val waterGoal: Int
) {
    val averageDailyCalories: Int get() = if (days.isEmpty()) 0 else days.sumOf { it.caloriesConsumed } / days.size
    val daysWithinGoal: Int get() = days.count { it.caloriesConsumed in 1..calorieGoal }
    val daysOverGoal: Int get() = days.count { it.caloriesConsumed > calorieGoal }
    val totalCaloriesBurned: Int get() = days.sumOf { it.caloriesBurned }
    val totalExerciseSessions: Int get() = days.sumOf { it.exerciseSessions }
}

data class WeekBucket(
    val label: String,
    val averageCalories: Int
)

data class MonthlyStats(
    val days: List<DaySummary>,
    val calorieGoal: Int,
    val waterGoal: Int
) {
    val averageDailyCalories: Int get() = if (days.isEmpty()) 0 else days.sumOf { it.caloriesConsumed } / days.size
    val averageDailyWater: Int get() = if (days.isEmpty()) 0 else days.sumOf { it.waterMl } / days.size
    val exerciseDays: Int get() = days.count { it.exerciseSessions > 0 }
    val weeklyCalorieTrend: List<WeekBucket>
        get() = days.chunked(7).mapIndexed { index, chunk ->
            WeekBucket(
                label = "Wk ${index + 1}",
                averageCalories = if (chunk.isEmpty()) 0 else chunk.sumOf { it.caloriesConsumed } / chunk.size
            )
        }
}
