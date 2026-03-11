package com.aliumitalgan.remindup

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aliumitalgan.remindup.screens.ForgotPasswordScreen
import com.aliumitalgan.remindup.screens.GoalsScreenContent
import com.aliumitalgan.remindup.screens.HomeScreenContent
import com.aliumitalgan.remindup.screens.LoginScreenContent
import com.aliumitalgan.remindup.screens.OnboardingScreen
import com.aliumitalgan.remindup.screens.PasswordResetConfirmationScreen
import com.aliumitalgan.remindup.screens.ProgressScreenContent
import com.aliumitalgan.remindup.screens.RegisterScreenContent
import com.aliumitalgan.remindup.screens.EditCategoryScreen
import com.aliumitalgan.remindup.screens.NotificationsScreen
import com.aliumitalgan.remindup.screens.PersonalInformationScreen
import com.aliumitalgan.remindup.screens.SecurityScreen
import com.aliumitalgan.remindup.screens.AppearanceScreen
import com.aliumitalgan.remindup.screens.ChangePasswordScreen
import com.aliumitalgan.remindup.screens.GoalCelebrationScreen
import com.aliumitalgan.remindup.screens.SettingsScreenContent
import com.aliumitalgan.remindup.screens.SocialScreen
import com.aliumitalgan.remindup.screens.SplashScreen
import com.aliumitalgan.remindup.screens.SweetTaskDetailScreen
import com.aliumitalgan.remindup.ui.assistant.AiAssistantScreen
import com.aliumitalgan.remindup.ui.premium.PremiumScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgotPassword")
    object PasswordResetConfirmation : Screen("passwordResetConfirmation")
    object Home : Screen("home")
    object Goals : Screen("goals")
    object Analytic : Screen("analytic")
    object SweetTaskDetail : Screen("sweetTaskDetail/{goalId}") {
        fun createRoute(goalId: String): String = "sweetTaskDetail/$goalId"
    }
    object Reminders : Screen("reminders")
    object Settings : Screen("settings")
    object Social : Screen("social")
    object Assistant : Screen("assistant")
    object Premium : Screen("premium")
    object PersonalInfo : Screen("personalInfo")
    object Notifications : Screen("notifications")
    object Security : Screen("security")
    object ChangePassword : Screen("changePassword")
    object Appearance : Screen("appearance")
    object GoalCelebration : Screen("goalCelebration?goalId={goalId}&goalTitle={goalTitle}&bonusXp={bonusXp}") {
        fun createRoute(goalId: String, goalTitle: String, bonusXp: Int): String {
            return "goalCelebration?goalId=${Uri.encode(goalId)}&goalTitle=${Uri.encode(goalTitle)}&bonusXp=$bonusXp"
        }
    }
    object EditCategory : Screen("editCategory/{categoryId}") {
        fun createRoute(categoryId: String): String = "editCategory/$categoryId"
    }
    object NewCategory : Screen("newCategory")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreenContent(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreenContent(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onResetEmailSent = {
                    navController.navigate(Screen.PasswordResetConfirmation.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PasswordResetConfirmation.route) {
            PasswordResetConfirmationScreen(
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PasswordResetConfirmation.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreenContent(
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToProgress = { navController.navigate(Screen.Analytic.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                onNavigateToSocial = { navController.navigate(Screen.Social.route) }
            )
        }

        composable(Screen.Goals.route) {
            GoalsScreenContent(
                onNavigateToProgress = { navController.navigate(Screen.Analytic.route) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToSocial = { navController.navigate(Screen.Social.route) },
                onNavigateToSweetTaskDetail = { goalId ->
                    navController.navigate(Screen.SweetTaskDetail.createRoute(goalId))
                }
            )
        }

        composable(Screen.Analytic.route) {
            ProgressScreenContent(
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToSocial = { navController.navigate(Screen.Social.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SweetTaskDetail.route,
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId").orEmpty()
            SweetTaskDetailScreen(
                goalId = goalId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProgress = { navController.navigate(Screen.Analytic.route) },
                onNavigateToSocial = { navController.navigate(Screen.Social.route) }
            )
        }

        composable(Screen.Reminders.route) {
            com.aliumitalgan.remindup.ui.reminders.RemindersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToProgress = { navController.navigate(Screen.Analytic.route) }
            )
        }

        composable(Screen.Social.route) {
            SocialScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProgress = { navController.navigate(Screen.Analytic.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreenContent(
                onNavigateBack = { navController.popBackStack(Screen.Home.route, false) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToProgress = { navController.navigate(Screen.Analytic.route) },
                onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                onNavigateToSocial = { navController.navigate(Screen.Social.route) },
                onNavigateToPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToSecurity = { navController.navigate(Screen.Security.route) },
                onNavigateToAppearance = { navController.navigate(Screen.Appearance.route) }
            )
        }

        composable(Screen.PersonalInfo.route) {
            PersonalInformationScreen(
                onNavigateBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSocial = { navController.navigate(Screen.Social.route) }
            )
        }

        composable(Screen.Security.route) {
            SecurityScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogoutAll = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSocial = { navController.navigate(Screen.Social.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) }
            )
        }

        composable(Screen.Appearance.route) {
            AppearanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onPasswordUpdated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GoalCelebration.route,
            arguments = listOf(
                navArgument("goalId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("goalTitle") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("bonusXp") {
                    type = NavType.IntType
                    defaultValue = 25
                }
            )
        ) { backStackEntry ->
            val goalTitle = backStackEntry.arguments?.getString("goalTitle").orEmpty()
            val bonusXp = backStackEntry.arguments?.getInt("bonusXp") ?: 25

            GoalCelebrationScreen(
                goalTitle = goalTitle.ifBlank { "Goal" },
                bonusXp = bonusXp,
                onAwesome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.EditCategory.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
            EditCategoryScreen(
                categoryId = categoryId.ifEmpty { null },
                onNavigateBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Assistant.route) {
            AiAssistantScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
