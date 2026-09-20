package com.suthinee.calorietracker.data.repository

import com.suthinee.calorietracker.data.local.dao.ExerciseLogDao
import com.suthinee.calorietracker.data.local.dao.FavoriteExerciseDao
import com.suthinee.calorietracker.data.local.entity.ExerciseLogEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ExerciseRepository(
    private val exerciseLogDao: ExerciseLogDao,
    private val favoriteExerciseDao: FavoriteExerciseDao
) {
    fun observeForDate(date: LocalDate): Flow<List<ExerciseLogEntity>> = exerciseLogDao.observeForDate(date)

    fun observeForRange(start: LocalDate, end: LocalDate): Flow<List<ExerciseLogEntity>> =
        exerciseLogDao.observeForRange(start, end)

    fun observeTotalBurnedForDate(date: LocalDate): Flow<Int> = exerciseLogDao.observeTotalBurnedForDate(date)

    suspend fun log(entity: ExerciseLogEntity): Long = exerciseLogDao.insert(entity)

    suspend fun delete(entity: ExerciseLogEntity) = exerciseLogDao.delete(entity)

    fun observeFavorites(): Flow<List<FavoriteExerciseEntity>> = favoriteExerciseDao.observeAll()

    suspend fun saveFavorite(entity: FavoriteExerciseEntity): Long = favoriteExerciseDao.insert(entity)

    suspend fun updateFavorite(entity: FavoriteExerciseEntity) = favoriteExerciseDao.update(entity)

    suspend fun deleteFavorite(entity: FavoriteExerciseEntity) = favoriteExerciseDao.delete(entity)
}
