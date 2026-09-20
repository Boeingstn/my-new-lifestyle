package com.suthinee.calorietracker.ui.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.suthinee.calorietracker.ui.common.LocalAppContainer

@Composable
fun ExerciseScreen() {
    val container = LocalAppContainer.current
    val viewModel: ExerciseViewModel = viewModel(
        factory = viewModelFactory { initializer { ExerciseViewModel(container) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.form

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Today's exercise", style = MaterialTheme.typography.labelLarge)
                        Text("${state.totalBurned} kcal burned", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            if (state.favorites.isNotEmpty()) {
                item {
                    Text("Favorites", style = MaterialTheme.typography.labelLarge)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.favorites.forEach { favorite ->
                            AssistChip(
                                onClick = { viewModel.applyFavorite(favorite) },
                                label = { Text(favorite.activityName) }
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Add exercise", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = form.activityName,
                            onValueChange = viewModel::setActivityName,
                            label = { Text("Activity") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = form.durationText,
                            onValueChange = viewModel::setDuration,
                            label = { Text("Duration (minutes)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = form.caloriesText,
                            onValueChange = viewModel::setCalories,
                            label = { Text("Estimated calories burned") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = viewModel::save,
                            enabled = form.canSave,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = viewModel::saveAsFavorite,
                            enabled = form.canSave,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save as favorite")
                        }
                    }
                }
            }

            item {
                Text("Today's log", style = MaterialTheme.typography.titleMedium)
            }
            items(state.todayLogs) { log ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                    IconButton(onClick = { viewModel.deleteLog(log) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}
