package com.suthinee.calorietracker.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.entity.ReminderEntity
import com.suthinee.calorietracker.data.local.entity.ReminderSettingsEntity
import com.suthinee.calorietracker.domain.model.ReminderType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

data class RemindersUiState(
    val reminders: List<ReminderEntity> = emptyList(),
    val quietHours: ReminderSettingsEntity = ReminderSettingsEntity.DEFAULT
)

class RemindersViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<RemindersUiState> = combine(
        container.reminderRepository.observeAll(),
        container.reminderRepository.observeSettings()
    ) { reminders, settings ->
        RemindersUiState(
            reminders = reminders.sortedBy { it.type.ordinal },
            quietHours = settings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RemindersUiState())

    fun setEnabled(type: ReminderType, enabled: Boolean) {
        viewModelScope.launch {
            val current = container.reminderRepository.getByType(type) ?: return@launch
            val updated = current.copy(enabled = enabled)
            container.reminderRepository.upsert(updated)
            if (enabled) container.reminderScheduler.schedule(updated) else container.reminderScheduler.cancel(type)
        }
    }

    fun setTime(type: ReminderType, time: LocalTime) {
        viewModelScope.launch {
            val current = container.reminderRepository.getByType(type) ?: return@launch
            val updated = current.copy(time = time)
            container.reminderRepository.upsert(updated)
            if (updated.enabled) container.reminderScheduler.schedule(updated)
        }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.reminderRepository.updateQuietHours(uiState.value.quietHours.copy(quietHoursEnabled = enabled))
        }
    }

    fun setQuietHoursRange(start: LocalTime, end: LocalTime) {
        viewModelScope.launch {
            container.reminderRepository.updateQuietHours(
                uiState.value.quietHours.copy(quietHoursStart = start, quietHoursEnd = end)
            )
        }
    }
}
