package com.suthinee.calorietracker

import android.content.Context
import com.suthinee.calorietracker.data.local.AppDatabase
import com.suthinee.calorietracker.data.repository.ExerciseRepository
import com.suthinee.calorietracker.data.repository.FoodRepository
import com.suthinee.calorietracker.data.repository.GoalRepository
import com.suthinee.calorietracker.data.repository.ReminderRepository
import com.suthinee.calorietracker.data.repository.WaterRepository
import com.suthinee.calorietracker.notification.ReminderScheduler

/** Simple hand-rolled dependency container; avoids pulling in a DI framework for a single-module personal app. */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val foodRepository = FoodRepository(database.foodLogDao(), database.favoriteFoodDao())
    val waterRepository = WaterRepository(database.waterLogDao())
    val exerciseRepository = ExerciseRepository(database.exerciseLogDao(), database.favoriteExerciseDao())
    val goalRepository = GoalRepository(database.dailyGoalDao())
    val reminderRepository = ReminderRepository(database.reminderDao(), database.reminderSettingsDao())
    val reminderScheduler = ReminderScheduler(context.applicationContext, reminderRepository)
}
