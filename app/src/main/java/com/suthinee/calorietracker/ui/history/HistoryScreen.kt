package com.suthinee.calorietracker.ui.history

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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.suthinee.calorietracker.ui.common.SimpleBarChart
import java.time.format.DateTimeFormatter

private enum class HistoryTab { WEEKLY, MONTHLY }
private val dayLabelFormatter = DateTimeFormatter.ofPattern("EEE")
private val dateLabelFormatter = DateTimeFormatter.ofPattern("MM/dd")

@Composable
fun HistoryScreen() {
    val container = LocalAppContainer.current
    val viewModel: HistoryViewModel = viewModel(
        factory = viewModelFactory { initializer { HistoryViewModel(container) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(HistoryTab.WEEKLY) }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab.ordinal) {
                Tab(selected = tab == HistoryTab.WEEKLY, onClick = { tab = HistoryTab.WEEKLY }, text = { Text("Weekly") })
                Tab(selected = tab == HistoryTab.MONTHLY, onClick = { tab = HistoryTab.MONTHLY }, text = { Text("Monthly") })
            }
            if (tab == HistoryTab.WEEKLY) {
                WeeklyContent(state.weekly)
            } else {
                MonthlyContent(state.monthly)
            }
        }
    }
}

@Composable
private fun WeeklyContent(stats: WeeklyStats) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SummaryCard(
                title = "Average daily calories",
                value = "${stats.averageDailyCalories} / ${stats.calorieGoal} kcal"
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(title = "Days within goal", value = "${stats.daysWithinGoal}", modifier = Modifier.weight(1f))
                SummaryCard(title = "Days over goal", value = "${stats.daysOverGoal}", modifier = Modifier.weight(1f))
            }
        }
        item {
            Text("Water intake trend (ml)", style = MaterialTheme.typography.titleMedium)
            SimpleBarChart(stats.days.map { it.date.format(dayLabelFormatter) to it.waterMl })
        }
        item {
            Text("Exercise summary", style = MaterialTheme.typography.titleMedium)
            SummaryCard(
                title = "Calories burned this week",
                value = "${stats.totalCaloriesBurned} kcal (${stats.totalExerciseSessions} sessions)"
            )
        }
    }
}

@Composable
private fun MonthlyContent(stats: MonthlyStats) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SummaryCard(
                title = "Average daily calories",
                value = "${stats.averageDailyCalories} / ${stats.calorieGoal} kcal"
            )
        }
        item {
            Text("Weekly calorie trend", style = MaterialTheme.typography.titleMedium)
            SimpleBarChart(stats.weeklyCalorieTrend.map { it.label to it.averageCalories })
        }
        item {
            Text("Water trend (avg ${stats.averageDailyWater} ml/day)", style = MaterialTheme.typography.titleMedium)
            SimpleBarChart(stats.days.chunked(3).map { chunk ->
                chunk.first().date.format(dateLabelFormatter) to (chunk.sumOf { it.waterMl } / chunk.size)
            })
        }
        item {
            SummaryCard(title = "Exercise frequency", value = "${stats.exerciseDays} / ${stats.days.size} days")
        }
        item {
            Text("Historical records", style = MaterialTheme.typography.titleMedium)
        }
        items(stats.days.sortedByDescending { it.date }) { day ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(day.date.format(dateLabelFormatter))
                Text("${day.caloriesConsumed} kcal · ${day.waterMl} ml · ${day.caloriesBurned} kcal burned")
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
