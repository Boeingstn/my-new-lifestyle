package com.suthinee.calorietracker.data.repository

import com.suthinee.calorietracker.data.local.dao.ReminderDao
import com.suthinee.calorietracker.data.local.dao.ReminderSettingsDao
import com.suthinee.calorietracker.data.local.entity.ReminderEntity
import com.suthinee.calorietracker.data.local.entity.ReminderSettingsEntity
import com.suthinee.calorietracker.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val reminderSettingsDao: ReminderSettingsDao
) {
    fun observeAll(): Flow<List<ReminderEntity>> = reminderDao.observeAll()

    fun observeSettings(): Flow<ReminderSettingsEntity> =
        reminderSettingsDao.observe().map { it ?: ReminderSettingsEntity.DEFAULT }

    suspend fun getByType(type: ReminderType): ReminderEntity? = reminderDao.getByType(type)

    suspend fun upsert(entity: ReminderEntity) = reminderDao.upsert(entity)

    suspend fun updateQuietHours(settings: ReminderSettingsEntity) = reminderSettingsDao.upsert(settings)

    suspend fun ensureDefaults() {
        if (reminderDao.count() == 0) {
            ReminderEntity.defaults().forEach { reminderDao.upsert(it) }
        }
    }
}
