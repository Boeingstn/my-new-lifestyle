package com.suthinee.calorietracker.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.entity.ExerciseLogEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExerciseFormState(
    val activityName: String = "",
    val durationText: String = "",
    val caloriesText: String = ""
) {
    val duration: Int? get() = durationText.toIntOrNull()
    val calories: Int? get() = caloriesText.toIntOrNull()
    val canSave: Boolean get() = activityName.isNotBlank() && duration != null && duration!! > 0 && calories != null && calories!! >= 0
}

data class ExerciseUiState(
    val todayLogs: List<ExerciseLogEntity> = emptyList(),
    val totalBurned: Int = 0,
    val favorites: List<FavoriteExerciseEntity> = emptyList(),
    val form: ExerciseFormState = ExerciseFormState()
)

class ExerciseViewModel(private val container: AppContainer) : ViewModel() {

    private val today = LocalDate.now()
    private val form = MutableStateFlow(ExerciseFormState())

    val uiState: StateFlow<ExerciseUiState> = combine(
        container.exerciseRepository.observeForDate(today),
        container.exerciseRepository.observeFavorites(),
        form
    ) { logs, favorites, form ->
        ExerciseUiState(
            todayLogs = logs,
            totalBurned = logs.sumOf { it.caloriesBurned },
            favorites = favorites,
            form = form
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseUiState())

    fun setActivityName(name: String) = form.update { it.copy(activityName = name) }
    fun setDuration(text: String) = form.update { it.copy(durationText = text.filter(Char::isDigit)) }
    fun setCalories(text: String) = form.update { it.copy(caloriesText = text.filter(Char::isDigit)) }

    fun applyFavorite(favorite: FavoriteExerciseEntity) = form.update {
        it.copy(
            activityName = favorite.activityName,
            durationText = favorite.defaultDuration.toString(),
            caloriesText = favorite.estimatedCalories.toString()
        )
    }

    fun save() {
        val current = form.value
        if (!current.canSave) return
        viewModelScope.launch {
            container.exerciseRepository.log(
                ExerciseLogEntity(
                    date = today,
                    activityName = current.activityName,
                    durationMinutes = current.duration ?: 0,
                    caloriesBurned = current.calories ?: 0
                )
            )
            form.value = ExerciseFormState()
        }
    }

    fun saveAsFavorite() {
        val current = form.value
        if (!current.canSave) return
        viewModelScope.launch {
            container.exerciseRepository.saveFavorite(
                FavoriteExerciseEntity(
                    activityName = current.activityName,
                    defaultDuration = current.duration ?: 0,
                    estimatedCalories = current.calories ?: 0
                )
            )
        }
    }

    fun deleteLog(entity: ExerciseLogEntity) {
        viewModelScope.launch { container.exerciseRepository.delete(entity) }
    }
}
