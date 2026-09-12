package com.spycodedoodledates

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spycodedoodledates.ui.home.HomeScreen
import com.spycodedoodledates.ui.navigation.Screen
import com.spycodedoodledates.ui.settings.SettingsScreen
import com.spycodedoodledates.ui.theme.DoodleDatesTheme
import com.spycodedoodledates.ui.theme.settings.ThemeSettings
import com.spycodedoodledates.ui.workspace.WorkspaceScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var themeSettings: ThemeSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoodleDatesTheme(themeSettings = themeSettings) {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToWorkspace = { monthKey ->
                    navController.navigate(Screen.Workspace.createRoute(monthKey))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Workspace.route,
            arguments = listOf(navArgument("monthKey") { type = NavType.StringType })
        ) { backStackEntry ->
            val monthKey = backStackEntry.arguments?.getString("monthKey")
            WorkspaceScreen(
                monthKey = monthKey,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
