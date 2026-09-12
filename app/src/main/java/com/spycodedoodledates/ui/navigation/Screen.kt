package com.spycodedoodledates.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Workspace : Screen("workspace/{monthKey}") {
        fun createRoute(monthKey: String) = "workspace/$monthKey"
    }
}
