package com.suthinee.calorietracker.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.suthinee.calorietracker.data.local.entity.FavoriteExerciseEntity
import com.suthinee.calorietracker.data.local.entity.FavoriteFoodEntity
import com.suthinee.calorietracker.domain.model.MealType
import com.suthinee.calorietracker.ui.common.LocalAppContainer

private enum class FavoritesTab { FOODS, EXERCISES }

@Composable
fun FavoritesScreen() {
    val container = LocalAppContainer.current
    val viewModel: FavoritesViewModel = viewModel(
        factory = viewModelFactory { initializer { FavoritesViewModel(container) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var tab by remember { mutableStateOf(FavoritesTab.FOODS) }
    var editingFood by remember { mutableStateOf<FavoriteFoodEntity?>(null) }
    var showFoodDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<FavoriteExerciseEntity?>(null) }
    var showExerciseDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (tab == FavoritesTab.FOODS) {
                    editingFood = null
                    showFoodDialog = true
                } else {
                    editingExercise = null
                    showExerciseDialog = true
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add favorite")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab.ordinal) {
                Tab(selected = tab == FavoritesTab.FOODS, onClick = { tab = FavoritesTab.FOODS }, text = { Text("Foods") })
                Tab(selected = tab == FavoritesTab.EXERCISES, onClick = { tab = FavoritesTab.EXERCISES }, text = { Text("Exercises") })
            }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tab == FavoritesTab.FOODS) {
                    items(state.foods) { food ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(food.foodName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${food.defaultPortion} · ${food.calories} kcal" + (food.mealType?.let { " · ${it.displayName}" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row {
                                IconButton(onClick = { editingFood = food; showFoodDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { viewModel.deleteFood(food) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                } else {
                    items(state.exercises) { exercise ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(exercise.activityName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${exercise.defaultDuration} min · ${exercise.estimatedCalories} kcal",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row {
                                IconButton(onClick = { editingExercise = exercise; showExerciseDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { viewModel.deleteExercise(exercise) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFoodDialog) {
        FoodFavoriteDialog(
            existing = editingFood,
            onDismiss = { showFoodDialog = false },
            onSave = { name, portion, calories, mealType ->
                viewModel.addOrUpdateFood(editingFood?.id ?: 0, name, portion, calories, mealType)
                showFoodDialog = false
            }
        )
    }

    if (showExerciseDialog) {
        ExerciseFavoriteDialog(
            existing = editingExercise,
            onDismiss = { showExerciseDialog = false },
            onSave = { name, duration, calories ->
                viewModel.addOrUpdateExercise(editingExercise?.id ?: 0, name, duration, calories)
                showExerciseDialog = false
            }
        )
    }
}

@Composable
private fun FoodFavoriteDialog(
    existing: FavoriteFoodEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, MealType?) -> Unit
) {
    var name by remember { mutableStateOf(existing?.foodName ?: "") }
    var portion by remember { mutableStateOf(existing?.defaultPortion ?: "1 จาน") }
    var caloriesText by remember { mutableStateOf(existing?.calories?.toString() ?: "") }
    var mealType by remember { mutableStateOf(existing?.mealType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add favorite food" else "Edit favorite food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food name") })
                OutlinedTextField(value = portion, onValueChange = { portion = it }, label = { Text("Default portion") })
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it.filter(Char::isDigit) },
                    label = { Text("Calories") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = mealType == type,
                            onClick = { mealType = if (mealType == type) null else type },
                            label = { Text(type.displayName) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { caloriesText.toIntOrNull()?.let { onSave(name, portion, it, mealType) } },
                enabled = name.isNotBlank() && caloriesText.toIntOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ExerciseFavoriteDialog(
    existing: FavoriteExerciseEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf(existing?.activityName ?: "") }
    var durationText by remember { mutableStateOf(existing?.defaultDuration?.toString() ?: "") }
    var caloriesText by remember { mutableStateOf(existing?.estimatedCalories?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add favorite exercise" else "Edit favorite exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Activity") })
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter(Char::isDigit) },
                    label = { Text("Default duration (min)") }
                )
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it.filter(Char::isDigit) },
                    label = { Text("Estimated calories burned") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = durationText.toIntOrNull()
                    val calories = caloriesText.toIntOrNull()
                    if (duration != null && calories != null) onSave(name, duration, calories)
                },
                enabled = name.isNotBlank() && durationText.toIntOrNull() != null && caloriesText.toIntOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
