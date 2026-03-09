package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.mainBottomNavItems

private val LightBg = Color(0xFFFDFBF9)
private val AccentOrange = Color(0xFFF26522)
private val Deep = Color(0xFF1A1A1A)
private val LightOrange = Color(0xFFFFF3E8)
private val LightMint = Color(0xFFE8F5E9)
private val QuietModeBg = Color(0xFF1E3A5F)

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {}
) {
    var dailyReminders by remember { mutableStateOf(true) }
    var goalAchievements by remember { mutableStateOf(true) }
    var taskUpdates by remember { mutableStateOf(false) }
    var systemAnnouncements by remember { mutableStateOf(true) }
    var quietModeEnabled by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("settings") }
    val navItems = mainBottomNavItems()

    Scaffold(
        containerColor = LightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Deep)
                }
                Text("Notifications", fontWeight = FontWeight.Bold, color = Deep, fontSize = 18.sp)
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Deep)
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> onNavigateToGoals()
                        "social" -> onNavigateToSocial()
                        "analytic" -> { }
                        "settings" -> onNavigateToSettings()
                    }
                },
                onCenterActionClick = onNavigateToGoals
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = LightOrange
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text("Status: Active", fontWeight = FontWeight.Bold, color = AccentOrange, fontSize = 16.sp)
                            Text("You have 4 types of alerts enabled.", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            }

            item {
                Text(
                    "ALERT PREFERENCES",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                NotificationPrefRow(
                    icon = Icons.Filled.CalendarMonth,
                    iconBg = LightOrange,
                    title = "Daily Reminders",
                    subtitle = "Morning summary of your tasks",
                    checked = dailyReminders,
                    onCheckedChange = { dailyReminders = it }
                )
            }
            item {
                NotificationPrefRow(
                    icon = Icons.Filled.EmojiEvents,
                    iconBg = LightMint,
                    title = "Goal Achievements",
                    subtitle = "Milestone celebration alerts",
                    checked = goalAchievements,
                    onCheckedChange = { goalAchievements = it }
                )
            }
            item {
                NotificationPrefRow(
                    icon = Icons.Filled.Checklist,
                    iconBg = LightOrange,
                    title = "Task Updates",
                    subtitle = "Notify when changes are made",
                    checked = taskUpdates,
                    onCheckedChange = { taskUpdates = it }
                )
            }
            item {
                NotificationPrefRow(
                    icon = Icons.Filled.Campaign,
                    iconBg = LightMint,
                    title = "System Announcements",
                    subtitle = "Important news and app updates",
                    checked = systemAnnouncements,
                    onCheckedChange = { systemAnnouncements = it }
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = QuietModeBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Quiet Mode", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Disable all alerts from 10 PM", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                        }
                        Surface(
                            onClick = { quietModeEnabled = !quietModeEnabled },
                            shape = RoundedCornerShape(12.dp),
                            color = AccentOrange
                        ) {
                            Text(
                                if (quietModeEnabled) "Disable" else "Enable",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun NotificationPrefRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF4A4A4A), modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Deep, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentOrange
                )
            )
        }
    }
}
