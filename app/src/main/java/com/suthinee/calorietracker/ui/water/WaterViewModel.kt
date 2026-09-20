package com.suthinee.calorietracker.ui.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.dao.DailyTotal
import com.suthinee.calorietracker.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WaterUiState(
    val waterGoal: Int = 2000,
    val todayTotal: Int = 0,
    val todayLogs: List<WaterLogEntity> = emptyList(),
    val recentHistory: List<DailyTotal> = emptyList()
)

class WaterViewModel(private val container: AppContainer) : ViewModel() {

    private val today = LocalDate.now()
    private val historyStart = today.minusDays(6)

    val uiState: StateFlow<WaterUiState> = combine(
        container.goalRepository.observeGoal(),
        container.waterRepository.observeTotalForDate(today),
        container.waterRepository.observeForDate(today),
        container.waterRepository.observeDailyTotalsForRange(historyStart, today)
    ) { goal, total, logs, history ->
        WaterUiState(
            waterGoal = goal.waterGoal,
            todayTotal = total,
            todayLogs = logs,
            recentHistory = history.sortedByDescending { it.date }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaterUiState())

    fun addWater(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch { container.waterRepository.addWater(amountMl, today) }
    }

    fun deleteLog(entity: WaterLogEntity) {
        viewModelScope.launch { container.waterRepository.delete(entity) }
    }
}
