package com.suthinee.calorietracker.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.entity.DailyGoalEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val goal: StateFlow<DailyGoalEntity> = container.goalRepository.observeGoal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyGoalEntity.DEFAULT)

    fun setGoal(calorieGoal: Int, waterGoal: Int) {
        if (calorieGoal <= 0 || waterGoal <= 0) return
        viewModelScope.launch { container.goalRepository.setGoal(calorieGoal, waterGoal) }
    }
}
