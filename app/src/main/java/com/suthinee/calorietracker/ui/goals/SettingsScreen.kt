package com.suthinee.calorietracker.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.suthinee.calorietracker.ui.common.LocalAppContainer

@Composable
fun SettingsScreen(
    onOpenFavorites: () -> Unit,
    onOpenReminders: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { initializer { SettingsViewModel(container) } }
    )
    val goal by viewModel.goal.collectAsStateWithLifecycle()

    var calorieText by remember { mutableStateOf("") }
    var waterText by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(goal) {
        if (!initialized) {
            calorieText = goal.calorieGoal.toString()
            waterText = goal.waterGoal.toString()
            initialized = true
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily goals", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = calorieText,
                        onValueChange = { calorieText = it.filter(Char::isDigit) },
                        label = { Text("Calorie goal (kcal)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = waterText,
                        onValueChange = { waterText = it.filter(Char::isDigit) },
                        label = { Text("Water goal (ml)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val calories = calorieText.toIntOrNull()
                            val water = waterText.toIntOrNull()
                            if (calories != null && water != null) viewModel.setGoal(calories, water)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save goals")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Favorites") },
                        supportingContent = { Text("Manage favorite foods & exercises") },
                        leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = onOpenFavorites)
                    )
                    ListItem(
                        headlineContent = { Text("Reminders") },
                        supportingContent = { Text("Meal & water notifications") },
                        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = onOpenReminders)
                    )
                }
            }
        }
    }
}
