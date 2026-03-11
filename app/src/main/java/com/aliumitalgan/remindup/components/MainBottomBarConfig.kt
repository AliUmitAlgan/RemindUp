package com.aliumitalgan.remindup.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import com.aliumitalgan.remindup.screens.BottomNavItem

@Composable
fun mainBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem("HOME", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        BottomNavItem("GOALS", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "goals"),
        BottomNavItem("FRIENDS", Icons.Filled.People, Icons.Outlined.People, "social"),
        BottomNavItem("ANALYTIC", Icons.Filled.EventNote, Icons.Outlined.EventNote, "analytic"),
        BottomNavItem("PROFILE", Icons.Filled.Person, Icons.Outlined.Person, "settings")
    )
}
