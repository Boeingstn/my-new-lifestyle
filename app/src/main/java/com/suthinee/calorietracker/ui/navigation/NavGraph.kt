package com.suthinee.calorietracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.suthinee.calorietracker.domain.model.MealType
import com.suthinee.calorietracker.ui.dashboard.DashboardScreen
import com.suthinee.calorietracker.ui.exercise.ExerciseScreen
import com.suthinee.calorietracker.ui.favorites.FavoritesScreen
import com.suthinee.calorietracker.ui.food.AddFoodScreen
import com.suthinee.calorietracker.ui.goals.SettingsScreen
import com.suthinee.calorietracker.ui.history.HistoryScreen
import com.suthinee.calorietracker.ui.reminders.RemindersScreen
import com.suthinee.calorietracker.ui.water.WaterScreen

private fun tabIcon(route: String) = when (route) {
    Screen.Dashboard.route -> Icons.Default.Today
    Screen.Water.route -> Icons.Default.LocalDrink
    Screen.Exercise.route -> Icons.Default.DirectionsRun
    Screen.History.route -> Icons.Default.CalendarMonth
    else -> Icons.Default.Settings
}

@Composable
fun CalorieTrackerNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar {
                Screen.bottomBarScreens.forEach { screen ->
                    val selected = currentRoute?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tabIcon(screen.route), contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddFood = { mealType ->
                        navController.navigate("${Screen.AddFood.route}/${mealType.name}")
                    },
                    onOpenWater = { navController.navigate(Screen.Water.route) },
                    onOpenExercise = { navController.navigate(Screen.Exercise.route) }
                )
            }
            composable(Screen.Water.route) { WaterScreen() }
            composable(Screen.Exercise.route) { ExerciseScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onOpenFavorites = { navController.navigate(Screen.Favorites.route) },
                    onOpenReminders = { navController.navigate(Screen.Reminders.route) }
                )
            }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Reminders.route) { RemindersScreen() }
            composable(
                route = "${Screen.AddFood.route}/{mealType}",
                arguments = listOf(navArgument("mealType") { defaultValue = MealType.SNACK.name })
            ) { backStackEntry ->
                val mealTypeArg = backStackEntry.arguments?.getString("mealType") ?: MealType.SNACK.name
                val mealType = runCatching { MealType.valueOf(mealTypeArg) }.getOrDefault(MealType.SNACK)
                AddFoodScreen(
                    initialMealType = mealType,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
