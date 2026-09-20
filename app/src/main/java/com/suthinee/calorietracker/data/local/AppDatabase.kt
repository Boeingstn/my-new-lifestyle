package com.suthinee.calorietracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.suthinee.calorietracker.data.local.dao.DailyGoalDao
import com.suthinee.calorietracker.data.local.dao.ExerciseLogDao
import com.suthinee.calorietracker.data.local.dao.FavoriteExerciseDao
import com.suthinee.calorietracker.data.local.dao.FavoriteFoodDao
import com.suthinee.calorietracker.data.local.dao.FoodLogDao
import com.suthinee.calorietracker.data.local.dao.ReminderDao
import com.suthinee.calorietracker.data.local.dao.ReminderSettingsDao
import com.suthinee.calorietracker.data.local.dao.WaterLogDao
import com.suthinee.calorietracker.data.local.entity.DailyGoalEntity
import com.suthinee.calorietracker.data.local.entity.ExerciseLogEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import com.suthinee.calorietracker.data.local.entity.FoodLogEntity
import com.suthinee.calorietracker.data.local.entity.ReminderEntity
import com.suthinee.calorietracker.data.local.entity.ReminderSettingsEntity
import com.suthinee.calorietracker.data.local.entity.WaterLogEntity

/**
 * Room schema version history (bump on any entity change and add a Migration
 * in [MIGRATIONS] so existing user data survives app updates):
 *  - 1: initial MVP schema
 */
@Database(
    entities = [
        FoodLogEntity::class,
        WaterLogEntity::class,
        ExerciseLogEntity::class,
        FavoriteFoodEntity::class,
        FavoriteExerciseEntity::class,
        DailyGoalEntity::class,
        ReminderEntity::class,
        ReminderSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodLogDao(): FoodLogDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun favoriteFoodDao(): FavoriteFoodDao
    abstract fun favoriteExerciseDao(): FavoriteExerciseDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao

    companion object {
        private const val DATABASE_NAME = "calorie_tracker.db"

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Future schema changes must add a Migration here instead of falling back to
         * destructive migration, so app updates never wipe existing logs.
         */
        private val MIGRATIONS: Array<androidx.room.migration.Migration> = arrayOf()

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*MIGRATIONS)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
