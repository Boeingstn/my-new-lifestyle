package com.suthinee.calorietracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.entity.ExerciseLogEntity
import com.suthinee.calorietracker.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

private const val MONTH_WINDOW_DAYS = 30L
private const val WEEK_WINDOW_DAYS = 7L

data class HistoryUiState(
    val weekly: WeeklyStats = WeeklyStats(emptyList(), 2000, 2000),
    val monthly: MonthlyStats = MonthlyStats(emptyList(), 2000, 2000)
)

class HistoryViewModel(container: AppContainer) : ViewModel() {

    private val today = LocalDate.now()
    private val monthStart = today.minusDays(MONTH_WINDOW_DAYS - 1)
    private val weekStart = today.minusDays(WEEK_WINDOW_DAYS - 1)

    val uiState: StateFlow<HistoryUiState> = combine(
        container.goalRepository.observeGoal(),
        container.foodRepository.observeForRange(monthStart, today),
        container.waterRepository.observeDailyTotalsForRange(monthStart, today),
        container.exerciseRepository.observeForRange(monthStart, today)
    ) { goal, foodLogs, waterTotals, exerciseLogs ->
        val allDays = buildDaySummaries(monthStart, today, foodLogs, waterTotals, exerciseLogs)
        val weeklyDays = allDays.filter { !it.date.isBefore(weekStart) }
        HistoryUiState(
            weekly = WeeklyStats(weeklyDays, goal.calorieGoal, goal.waterGoal),
            monthly = MonthlyStats(allDays, goal.calorieGoal, goal.waterGoal)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    private fun buildDaySummaries(
        start: LocalDate,
        end: LocalDate,
        foodLogs: List<FoodLogEntity>,
        waterTotals: List<com.suthinee.calorietracker.data.local.dao.DailyTotal>,
        exerciseLogs: List<ExerciseLogEntity>
    ): List<DaySummary> {
        val caloriesByDate = foodLogs.groupBy { it.date }.mapValues { (_, logs) -> logs.sumOf { it.calories } }
        val waterByDate = waterTotals.associate { it.date to it.total }
        val exerciseByDate = exerciseLogs.groupBy { it.date }

        val days = mutableListOf<DaySummary>()
        var date = start
        while (!date.isAfter(end)) {
            val exercisesForDate = exerciseByDate[date].orEmpty()
            days.add(
                DaySummary(
                    date = date,
                    caloriesConsumed = caloriesByDate[date] ?: 0,
                    waterMl = waterByDate[date] ?: 0,
                    exerciseSessions = exercisesForDate.size,
                    caloriesBurned = exercisesForDate.sumOf { it.caloriesBurned }
                )
            )
            date = date.plusDays(1)
        }
        return days
    }
}
