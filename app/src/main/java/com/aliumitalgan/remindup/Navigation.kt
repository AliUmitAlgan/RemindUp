package com.aliumitalgan.remindup

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliumitalgan.remindup.screens.*

// Navigation rotaları
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Goals : Screen("goals")
    object Progress : Screen("progress")
    object Reminders : Screen("reminders")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreenContent(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.Register.route) {
            RegisterScreenContent(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.Home.route) {
            HomeScreenContent(
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Goals.route) {
            GoalsScreenContent(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreenContent(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reminders.route) {
            RemindersScreenContent(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Yeni eklenen Settings ekranı
        composable(Screen.Settings.route) {
            SettingsScreenContent(
                onNavigateBack = {
                    // Geri gitme işlemini kesin bir şekilde tanımlayın
                    navController.popBackStack(Screen.Home.route, false)
                }
            )
        }
    }
}