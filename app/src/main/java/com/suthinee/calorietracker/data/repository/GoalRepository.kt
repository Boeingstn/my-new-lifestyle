package com.suthinee.calorietracker.data.repository

import com.suthinee.calorietracker.data.local.dao.DailyGoalDao
import com.suthinee.calorietracker.data.local.entity.DailyGoalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(
    private val dailyGoalDao: DailyGoalDao
) {
    fun observeGoal(): Flow<DailyGoalEntity> = dailyGoalDao.observe().map { it ?: DailyGoalEntity.DEFAULT }

    suspend fun setGoal(calorieGoal: Int, waterGoal: Int) {
        dailyGoalDao.upsert(DailyGoalEntity(calorieGoal = calorieGoal, waterGoal = waterGoal))
    }
}
