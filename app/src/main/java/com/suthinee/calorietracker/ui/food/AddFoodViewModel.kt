package com.suthinee.calorietracker.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suthinee.calorietracker.AppContainer
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import com.suthinee.calorietracker.data.local.entity.FoodLogEntity
import com.suthinee.calorietracker.domain.model.MealType
import com.suthinee.calorietracker.domain.nlp.FoodDescriptionParser
import com.suthinee.calorietracker.domain.nlp.ParsedFoodDescription
import com.suthinee.calorietracker.domain.nlp.PortionOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AddFoodTab { PHOTO, DESCRIBE, MANUAL }

data class AddFoodFormState(
    val tab: AddFoodTab = AddFoodTab.MANUAL,
    val mealType: MealType = MealType.BREAKFAST,
    val foodName: String = "",
    val portion: String = "1 จาน",
    val caloriesText: String = "",
    val photoPath: String? = null,
    val describeText: String = "",
    val parsedDescription: ParsedFoodDescription? = null,
    val answeredPortionOption: PortionOption? = null,
    val saved: Boolean = false
) {
    val calories: Int? get() = caloriesText.toIntOrNull()
    val canSave: Boolean get() = foodName.isNotBlank() && calories != null && calories!! > 0
}

class AddFoodViewModel(
    private val container: AppContainer,
    initialMealType: MealType
) : ViewModel() {

    private val form = MutableStateFlow(AddFoodFormState(mealType = initialMealType))

    val uiState: StateFlow<AddFoodUiState> = combine(
        form,
        container.foodRepository.observeFavorites()
    ) { form, favorites ->
        AddFoodUiState(form = form, favorites = favorites)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AddFoodUiState(form.value, emptyList())
    )

    fun selectTab(tab: AddFoodTab) = form.update { it.copy(tab = tab) }

    fun setMealType(mealType: MealType) = form.update { it.copy(mealType = mealType) }

    fun setFoodName(name: String) = form.update { it.copy(foodName = name) }

    fun setPortion(portion: String) = form.update { it.copy(portion = portion) }

    fun setCalories(text: String) = form.update { it.copy(caloriesText = text.filter { c -> c.isDigit() }) }

    fun setPhotoPath(path: String?) = form.update {
        it.copy(photoPath = path, foodName = it.foodName.ifBlank { "Meal photo" })
    }

    fun setDescribeText(text: String) = form.update { it.copy(describeText = text, parsedDescription = null) }

    fun parseDescription() {
        val text = form.value.describeText
        if (text.isBlank()) return
        val parsed = FoodDescriptionParser.parse(text)
        form.update {
            it.copy(
                parsedDescription = parsed,
                answeredPortionOption = null,
                foodName = parsed.suggestedName,
                caloriesText = parsed.defaultEstimatedCalories.toString(),
                portion = "1 จาน"
            )
        }
    }

    fun answerPortionQuestion(option: PortionOption) {
        val parsed = form.value.parsedDescription ?: return
        form.update {
            it.copy(
                answeredPortionOption = option,
                caloriesText = parsed.estimatedCalories(option.multiplier).toString(),
                portion = option.label
            )
        }
    }

    fun skipPortionQuestion() {
        val parsed = form.value.parsedDescription ?: return
        form.update {
            it.copy(
                answeredPortionOption = null,
                caloriesText = parsed.defaultEstimatedCalories.toString(),
                portion = "ไม่แน่ใจ"
            )
        }
    }

    fun applyFavorite(favorite: FavoriteFoodEntity) = form.update {
        it.copy(
            foodName = favorite.foodName,
            portion = favorite.defaultPortion,
            caloriesText = favorite.calories.toString(),
            mealType = favorite.mealType ?: it.mealType
        )
    }

    fun saveAsFavorite() {
        val state = form.value
        if (!state.canSave) return
        viewModelScope.launch {
            container.foodRepository.saveFavorite(
                FavoriteFoodEntity(
                    foodName = state.foodName,
                    defaultPortion = state.portion,
                    calories = state.calories ?: 0,
                    mealType = state.mealType
                )
            )
        }
    }

    fun save() {
        val state = form.value
        if (!state.canSave) return
        viewModelScope.launch {
            container.foodRepository.log(
                FoodLogEntity(
                    date = LocalDate.now(),
                    mealType = state.mealType,
                    foodName = state.foodName,
                    portion = state.portion,
                    calories = state.calories ?: 0,
                    photoPath = state.photoPath
                )
            )
            form.update { it.copy(saved = true) }
        }
    }
}

data class AddFoodUiState(
    val form: AddFoodFormState,
    val favorites: List<FavoriteFoodEntity>
)
