package com.aliumitalgan.remindup.presentation.navigation

enum class MainTab(
    val route: String,
    val label: String
) {
    Home(route = "home", label = "Home"),
    Goals(route = "goals", label = "Goals"),
    Analytics(route = "analytics", label = "Analytics"),
    Profile(route = "profile", label = "Profile")
}
