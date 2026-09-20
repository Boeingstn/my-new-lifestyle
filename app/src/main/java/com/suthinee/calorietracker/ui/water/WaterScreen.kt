package com.suthinee.calorietracker.ui.water

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.suthinee.calorietracker.ui.common.LocalAppContainer
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun WaterScreen() {
    val container = LocalAppContainer.current
    val viewModel: WaterViewModel = viewModel(
        factory = viewModelFactory { initializer { WaterViewModel(container) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCustomDialog by remember { mutableStateOf(false) }

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
                        Text("Water Intake", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("${state.todayTotal} / ${state.waterGoal} ml", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        val progress = if (state.waterGoal > 0) {
                            (state.todayTotal.toFloat() / state.waterGoal.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(250, 500, 750).forEach { amount ->
                        OutlinedButton(
                            onClick = { viewModel.addWater(amount) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+$amount ml")
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Custom amount")
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
                    Text("${log.amountMl} ml", style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { viewModel.deleteLog(log) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            item {
                Text("Last 7 days", style = MaterialTheme.typography.titleMedium)
            }
            items(state.recentHistory) { day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                    Text("${day.total} ml")
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount ->
                viewModel.addWater(amount)
                showCustomDialog = false
            }
        )
    }
}

@Composable
private fun CustomAmountDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom amount") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter(Char::isDigit) },
                label = { Text("Amount (ml)") }
            )
        },
        confirmButton = {
            TextButton(onClick = { text.toIntOrNull()?.let(onConfirm) }, enabled = text.toIntOrNull() != null) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
