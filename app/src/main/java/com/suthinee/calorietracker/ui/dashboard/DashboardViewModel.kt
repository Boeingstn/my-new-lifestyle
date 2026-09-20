package com.suthinee.calorietracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.entity.ExerciseLogEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import com.suthinee.calorietracker.data.local.entity.FoodLogEntity
import com.suthinee.calorietracker.domain.model.MealType
import com.suthinee.calorietracker.domain.recommendation.DailyRecommendation
import com.suthinee.calorietracker.domain.recommendation.RecommendationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import com.suthinee.calorietracker.util.PhotoStorage
import java.time.LocalDate

private data class PartialDashboardData(
    val date: LocalDate,
    val calorieGoal: Int,
    val waterGoal: Int,
    val foodLogs: List<FoodLogEntity>,
    val waterMl: Int,
    val exerciseLogs: List<ExerciseLogEntity>,
    val favoriteFoods: List<FavoriteFoodEntity>
)

data class DashboardUiState(
    val date: LocalDate = LocalDate.now(),
    val calorieGoal: Int = 2000,
    val waterGoal: Int = 2000,
    val mealLogs: Map<MealType, List<FoodLogEntity>> = emptyMap(),
    val consumedCalories: Int = 0,
    val waterMl: Int = 0,
    val exerciseLogs: List<ExerciseLogEntity> = emptyList(),
    val caloriesBurned: Int = 0,
    val recommendation: DailyRecommendation? = null,
    val isLoading: Boolean = true
) {
    val remainingCalories: Int get() = calorieGoal - consumedCalories
    val isOverGoal: Boolean get() = remainingCalories < 0
}

class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<DashboardUiState> = selectedDate.flatMapLatest { date ->
        val partial = combine(
            container.goalRepository.observeGoal(),
            container.foodRepository.observeForDate(date),
            container.waterRepository.observeTotalForDate(date),
            container.exerciseRepository.observeForDate(date),
            container.foodRepository.observeFavorites()
        ) { goal, foodLogs, waterMl, exerciseLogs, favoriteFoods ->
            PartialDashboardData(
                date = date,
                calorieGoal = goal.calorieGoal,
                waterGoal = goal.waterGoal,
                foodLogs = foodLogs,
                waterMl = waterMl,
                exerciseLogs = exerciseLogs,
                favoriteFoods = favoriteFoods
            )
        }

        combine(partial, container.exerciseRepository.observeFavorites()) { data, favoriteExercises ->
            val consumed = data.foodLogs.sumOf { it.calories }
            val burned = data.exerciseLogs.sumOf { it.caloriesBurned }
            val recommendation = RecommendationEngine.recommend(
                consumedCalories = consumed,
                calorieGoal = data.calorieGoal,
                favoriteFoods = data.favoriteFoods,
                favoriteExercises = favoriteExercises
            )

            DashboardUiState(
                date = data.date,
                calorieGoal = data.calorieGoal,
                waterGoal = data.waterGoal,
                mealLogs = MealType.entries.associateWith { type -> data.foodLogs.filter { it.mealType == type } },
                consumedCalories = consumed,
                waterMl = data.waterMl,
                exerciseLogs = data.exerciseLogs,
                caloriesBurned = burned,
                recommendation = recommendation,
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun goToPreviousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        val next = selectedDate.value.plusDays(1)
        if (!next.isAfter(LocalDate.now())) {
            selectedDate.value = next
        }
    }

    fun goToToday() {
        selectedDate.value = LocalDate.now()
    }

    suspend fun deleteFoodLog(entity: FoodLogEntity) {
        PhotoStorage.deletePhoto(entity.photoPath)
        container.foodRepository.delete(entity)
    }

    suspend fun deleteExerciseLog(entity: ExerciseLogEntity) {
        container.exerciseRepository.delete(entity)
    }
}
