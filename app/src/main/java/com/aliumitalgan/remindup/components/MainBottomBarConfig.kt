package com.aliumitalgan.remindup.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import com.aliumitalgan.remindup.screens.BottomNavItem

@Composable
fun mainBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem("HOME", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        BottomNavItem("TASKS", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "goals"),
        BottomNavItem("PLAN", Icons.Filled.EventNote, Icons.Outlined.EventNote, "progress"),
        BottomNavItem("CONFIG", Icons.Filled.Settings, Icons.Outlined.Settings, "settings")
    )
}
