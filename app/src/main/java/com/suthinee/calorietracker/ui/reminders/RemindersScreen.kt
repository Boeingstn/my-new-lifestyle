package com.suthinee.calorietracker.ui.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.suthinee.calorietracker.data.local.entity.ReminderEntity
import com.suthinee.calorietracker.ui.common.LocalAppContainer
import com.suthinee.calorietracker.ui.common.TimePickerDialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun RemindersScreen() {
    val container = LocalAppContainer.current
    val viewModel: RemindersViewModel = viewModel(
        factory = viewModelFactory { initializer { RemindersViewModel(container) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }
    var editingQuietStart by remember { mutableStateOf(false) }
    var editingQuietEnd by remember { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Meal & water reminders", style = MaterialTheme.typography.titleMedium)
            }
            items(state.reminders) { reminder ->
                ReminderRow(
                    reminder = reminder,
                    onToggle = { viewModel.setEnabled(reminder.type, it) },
                    onEditTime = { editingReminder = reminder }
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quiet hours", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = state.quietHours.quietHoursEnabled,
                                onCheckedChange = viewModel::setQuietHoursEnabled
                            )
                        }
                        Text(
                            "No reminders will be sent between these times.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { editingQuietStart = true }) {
                                Text("From ${state.quietHours.quietHoursStart.format(timeFormatter)}")
                            }
                            TextButton(onClick = { editingQuietEnd = true }) {
                                Text("To ${state.quietHours.quietHoursEnd.format(timeFormatter)}")
                            }
                        }
                    }
                }
            }
        }
    }

    editingReminder?.let { reminder ->
        TimePickerDialog(
            initialTime = reminder.time,
            onDismiss = { editingReminder = null },
            onConfirm = { time ->
                viewModel.setTime(reminder.type, time)
                editingReminder = null
            }
        )
    }

    if (editingQuietStart) {
        TimePickerDialog(
            initialTime = state.quietHours.quietHoursStart,
            onDismiss = { editingQuietStart = false },
            onConfirm = { time ->
                viewModel.setQuietHoursRange(time, state.quietHours.quietHoursEnd)
                editingQuietStart = false
            }
        )
    }

    if (editingQuietEnd) {
        TimePickerDialog(
            initialTime = state.quietHours.quietHoursEnd,
            onDismiss = { editingQuietEnd = false },
            onConfirm = { time ->
                viewModel.setQuietHoursRange(state.quietHours.quietHoursStart, time)
                editingQuietEnd = false
            }
        )
    }
}

@Composable
private fun ReminderRow(
    reminder: ReminderEntity,
    onToggle: (Boolean) -> Unit,
    onEditTime: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(reminder.type.displayName, style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = onEditTime) {
                    Text(reminder.time.format(timeFormatter))
                }
            }
            Switch(checked = reminder.enabled, onCheckedChange = onToggle)
        }
    }
}
