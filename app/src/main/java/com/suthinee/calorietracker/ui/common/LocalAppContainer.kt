package com.suthinee.calorietracker.ui.common

import androidx.compose.runtime.staticCompositionLocalOf
import com.suthinee.calorietracker.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
