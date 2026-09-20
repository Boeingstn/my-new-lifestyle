package com.suthinee.calorietracker.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Dashboard : Screen("dashboard", "Today")
    data object Water : Screen("water", "Water")
    data object Exercise : Screen("exercise", "Exercise")
    data object History : Screen("history", "History")
    data object Settings : Screen("settings", "Settings")

    data object AddFood : Screen("add_food", "Log Food")
    data object Favorites : Screen("favorites", "Favorites")
    data object Reminders : Screen("reminders", "Reminders")

    companion object {
        val bottomBarScreens = listOf(Dashboard, Water, Exercise, History, Settings)
    }
}
