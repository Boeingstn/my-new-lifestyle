package com.suthinee.calorietracker.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.suthinee.calorietracker.data.local.entity.ExerciseLogEntity
import com.suthinee.calorietracker.data.local.entity.FoodLogEntity
import com.suthinee.calorietracker.domain.model.MealType
import com.suthinee.calorietracker.domain.recommendation.DailyRecommendation
import com.suthinee.calorietracker.ui.common.LocalAppContainer
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

@Composable
fun DashboardScreen(
    onAddFood: (MealType) -> Unit,
    onOpenWater: () -> Unit,
    onOpenExercise: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory { initializer { DashboardViewModel(container) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DateNavigationRow(
                    dateLabel = state.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                    onPrevious = viewModel::goToPreviousDay,
                    onNext = viewModel::goToNextDay,
                    onToday = viewModel::goToToday
                )
            }
            item {
                CalorieSummaryCard(state = state)
            }
            item {
                WaterAndExerciseRow(
                    waterMl = state.waterMl,
                    waterGoal = state.waterGoal,
                    caloriesBurned = state.caloriesBurned,
                    onOpenWater = onOpenWater,
                    onOpenExercise = onOpenExercise
                )
            }
            state.recommendation?.let { recommendation ->
                item { RecommendationCard(recommendation) }
            }
            items(MealType.entries.toList()) { mealType ->
                MealSection(
                    mealType = mealType,
                    logs = state.mealLogs[mealType].orEmpty(),
                    onAdd = { onAddFood(mealType) },
                    onDelete = { entity -> scope.launch { viewModel.deleteFoodLog(entity) } }
                )
            }
            item {
                ExerciseSummarySection(
                    logs = state.exerciseLogs,
                    onAdd = onOpenExercise,
                    onDelete = { entity -> scope.launch { viewModel.deleteExerciseLog(entity) } }
                )
            }
        }
    }
}

@Composable
private fun DateNavigationRow(
    dateLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
        }
        TextButton(onClick = onToday) {
            Text(dateLabel, style = MaterialTheme.typography.titleMedium)
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
        }
    }
}

@Composable
private fun CalorieSummaryCard(state: DashboardUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Calorie Goal", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "${state.consumedCalories} / ${state.calorieGoal} kcal",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            val progress = if (state.calorieGoal > 0) {
                (state.consumedCalories.toFloat() / state.calorieGoal.toFloat()).coerceIn(0f, 1f)
            } else 0f
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.isOverGoal) {
                    "${abs(state.remainingCalories)} kcal over"
                } else {
                    "${state.remainingCalories} kcal remaining"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.isOverGoal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun WaterAndExerciseRow(
    waterMl: Int,
    waterGoal: Int,
    caloriesBurned: Int,
    onOpenWater: () -> Unit,
    onOpenExercise: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onClick = onOpenWater
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Water", style = MaterialTheme.typography.labelLarge)
                Text("$waterMl / $waterGoal ml", style = MaterialTheme.typography.titleMedium)
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onClick = onOpenExercise
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Exercise", style = MaterialTheme.typography.labelLarge)
                Text("$caloriesBurned kcal burned", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: DailyRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (recommendation) {
                is DailyRecommendation.RemainingBudget -> {
                    Text("You have ${recommendation.remainingCalories} kcal remaining today.")
                    if (recommendation.suggestedFoods.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Try: " + recommendation.suggestedFoods.joinToString { "${it.foodName} (${it.calories} kcal)" })
                    }
                }
                is DailyRecommendation.OverBudget -> {
                    Text("You are ${recommendation.excessCalories} kcal over your daily goal.")
                    if (recommendation.suggestedExercises.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Try: " + recommendation.suggestedExercises.joinToString { "${it.activityName} (${it.estimatedCalories} kcal)" })
                    }
                }
            }
        }
    }
}

@Composable
private fun MealSection(
    mealType: MealType,
    logs: List<FoodLogEntity>,
    onAdd: () -> Unit,
    onDelete: (FoodLogEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(mealType.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${logs.sumOf { it.calories }} kcal",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add ${mealType.displayName}")
                }
            }
            logs.forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(log.foodName, style = MaterialTheme.typography.bodyLarge)
                        Text("${log.portion} · ${log.calories} kcal", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDelete(log) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseSummarySection(
    logs: List<ExerciseLogEntity>,
    onAdd: () -> Unit,
    onDelete: (ExerciseLogEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Exercise", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add exercise")
                }
            }
            logs.forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(log.activityName, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${log.durationMinutes} min · ${log.caloriesBurned} kcal",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { onDelete(log) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}
