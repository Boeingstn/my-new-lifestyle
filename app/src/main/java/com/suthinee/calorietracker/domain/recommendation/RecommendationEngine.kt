package com.suthinee.calorietracker.domain.recommendation

import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import kotlin.math.abs

sealed class DailyRecommendation {
    data class RemainingBudget(
        val remainingCalories: Int,
        val suggestedFoods: List<FavoriteFoodEntity>
    ) : DailyRecommendation()

    data class OverBudget(
        val excessCalories: Int,
        val suggestedExercises: List<FavoriteExerciseEntity>
    ) : DailyRecommendation()
}

/** Simple rule-based recommendations from the user's own saved favorites; no external API involved. */
object RecommendationEngine {

    private const val MAX_SUGGESTIONS = 5

    fun recommend(
        consumedCalories: Int,
        calorieGoal: Int,
        favoriteFoods: List<FavoriteFoodEntity>,
        favoriteExercises: List<FavoriteExerciseEntity>
    ): DailyRecommendation {
        val remaining = calorieGoal - consumedCalories
        return if (remaining >= 0) {
            val suggestions = favoriteFoods
                .filter { it.calories <= remaining }
                .sortedByDescending { it.calories }
                .take(MAX_SUGGESTIONS)
                .ifEmpty { favoriteFoods.sortedBy { it.calories }.take(MAX_SUGGESTIONS) }
            DailyRecommendation.RemainingBudget(remaining, suggestions)
        } else {
            val excess = abs(remaining)
            val suggestions = favoriteExercises
                .filter { it.estimatedCalories >= excess }
                .sortedBy { it.estimatedCalories }
                .take(MAX_SUGGESTIONS)
                .ifEmpty { favoriteExercises.sortedByDescending { it.estimatedCalories }.take(MAX_SUGGESTIONS) }
            DailyRecommendation.OverBudget(excess, suggestions)
        }
    }
}
