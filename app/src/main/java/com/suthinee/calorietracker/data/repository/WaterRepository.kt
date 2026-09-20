package com.suthinee.calorietracker.data.repository

import com.suthinee.calorietracker.data.local.dao.DailyTotal
import com.suthinee.calorietracker.data.local.dao.WaterLogDao
import com.suthinee.calorietracker.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WaterRepository(
    private val waterLogDao: WaterLogDao
) {
    fun observeForDate(date: LocalDate): Flow<List<WaterLogEntity>> = waterLogDao.observeForDate(date)

    fun observeTotalForDate(date: LocalDate): Flow<Int> = waterLogDao.observeTotalForDate(date)

    fun observeDailyTotalsForRange(start: LocalDate, end: LocalDate): Flow<List<DailyTotal>> =
        waterLogDao.observeDailyTotalsForRange(start, end)

    suspend fun addWater(amountMl: Int, date: LocalDate = LocalDate.now()): Long =
        waterLogDao.insert(WaterLogEntity(date = date, amountMl = amountMl))

    suspend fun delete(entity: WaterLogEntity) = waterLogDao.delete(entity)
}
