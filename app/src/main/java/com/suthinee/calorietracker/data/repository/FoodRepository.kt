package com.suthinee.calorietracker.data.repository

import com.suthinee.calorietracker.data.local.dao.FavoriteFoodDao
import com.suthinee.calorietracker.data.local.dao.FoodLogDao
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import com.suthinee.calorietracker.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class FoodRepository(
    private val foodLogDao: FoodLogDao,
    private val favoriteFoodDao: FavoriteFoodDao
) {
    fun observeForDate(date: LocalDate): Flow<List<FoodLogEntity>> = foodLogDao.observeForDate(date)

    fun observeForRange(start: LocalDate, end: LocalDate): Flow<List<FoodLogEntity>> =
        foodLogDao.observeForRange(start, end)

    fun observeTotalCaloriesForDate(date: LocalDate): Flow<Int> =
        foodLogDao.observeTotalCaloriesForDate(date)

    suspend fun log(entity: FoodLogEntity): Long = foodLogDao.insert(entity)

    suspend fun update(entity: FoodLogEntity) = foodLogDao.update(entity)

    suspend fun delete(entity: FoodLogEntity) = foodLogDao.delete(entity)

    fun observeFavorites(): Flow<List<FavoriteFoodEntity>> = favoriteFoodDao.observeAll()

    suspend fun saveFavorite(entity: FavoriteFoodEntity): Long = favoriteFoodDao.insert(entity)

    suspend fun updateFavorite(entity: FavoriteFoodEntity) = favoriteFoodDao.update(entity)

    suspend fun deleteFavorite(entity: FavoriteFoodEntity) = favoriteFoodDao.delete(entity)
}
