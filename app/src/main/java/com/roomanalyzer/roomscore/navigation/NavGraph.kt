package com.roomanalyzer.roomscore.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Result : Screen("result")
    data object Settings : Screen("settings")
}
