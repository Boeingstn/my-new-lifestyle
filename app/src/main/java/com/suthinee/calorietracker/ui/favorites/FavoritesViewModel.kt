package com.suthinee.calorietracker.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import com.suthinee.calorietracker.domain.model.MealType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val foods: List<FavoriteFoodEntity> = emptyList(),
    val exercises: List<FavoriteExerciseEntity> = emptyList()
)

class FavoritesViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        container.foodRepository.observeFavorites(),
        container.exerciseRepository.observeFavorites()
    ) { foods, exercises ->
        FavoritesUiState(foods = foods, exercises = exercises)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesUiState())

    fun addOrUpdateFood(id: Long, name: String, portion: String, calories: Int, mealType: MealType?) {
        viewModelScope.launch {
            container.foodRepository.saveFavorite(
                FavoriteFoodEntity(id = id, foodName = name, defaultPortion = portion, calories = calories, mealType = mealType)
            )
        }
    }

    fun deleteFood(entity: FavoriteFoodEntity) {
        viewModelScope.launch { container.foodRepository.deleteFavorite(entity) }
    }

    fun addOrUpdateExercise(id: Long, name: String, duration: Int, calories: Int) {
        viewModelScope.launch {
            container.exerciseRepository.saveFavorite(
                FavoriteExerciseEntity(id = id, activityName = name, defaultDuration = duration, estimatedCalories = calories)
            )
        }
    }

    fun deleteExercise(entity: FavoriteExerciseEntity) {
        viewModelScope.launch { container.exerciseRepository.deleteFavorite(entity) }
    }
}
