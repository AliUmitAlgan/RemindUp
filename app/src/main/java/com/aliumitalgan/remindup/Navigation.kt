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
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },

            )
        }

        composable(Screen.Progress.route) {
            ProgressScreenContent(
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateBack = { navController.popBackStack() },

            )
        }

        composable(Screen.Reminders.route) {
            RemindersScreenContent(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreenContent(
                onNavigateBack = {
                    // geri ok tuşuna basınca Home’a dönsün:
                    navController.popBackStack(Screen.Home.route, false)
                },
                onLogout = {
                    // çıkış yapınca Login ekranına git ve geçmişi temizle
                    navController.navigate(Screen.Login.route) {
                        // uygulamanın back stack'ini tamamen temizleyelim:
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },

            )
        }

    }
}