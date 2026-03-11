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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.appCardSubtleColor
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.UserPreferenceUtils
import kotlinx.coroutines.launch

private val ScreenBg: Color
    get() = themedColor(Color(0xFFF6F4F4), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val Deep: Color
    get() = themedColor(Color(0xFF1C2635), Color(0xFFE5E7EB))

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
    var showActionsMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        UserPreferenceUtils.getNotificationPreferences()
            .onSuccess { prefs ->
                dailyReminders = prefs.dailyReminders
                goalAchievements = prefs.goalAchievements
                taskUpdates = prefs.taskUpdates
                systemAnnouncements = prefs.systemAnnouncements
                quietModeEnabled = prefs.quietModeEnabled
            }
        isLoading = false
    }

    fun updateNotificationField(field: String, nextValue: Boolean, updateLocal: (Boolean) -> Unit) {
        updateLocal(nextValue)
        scope.launch {
            UserPreferenceUtils.updateNotificationPreference(field, nextValue)
                .onFailure { updateLocal(!nextValue) }
        }
    }

    fun updateAllNotificationToggles(nextValue: Boolean) {
        updateNotificationField("dailyReminders", nextValue) { dailyReminders = it }
        updateNotificationField("goalAchievements", nextValue) { goalAchievements = it }
        updateNotificationField("taskUpdates", nextValue) { taskUpdates = it }
        updateNotificationField("systemAnnouncements", nextValue) { systemAnnouncements = it }
    }

    fun resetNotificationDefaults() {
        updateNotificationField("dailyReminders", true) { dailyReminders = it }
        updateNotificationField("goalAchievements", true) { goalAchievements = it }
        updateNotificationField("taskUpdates", false) { taskUpdates = it }
        updateNotificationField("systemAnnouncements", true) { systemAnnouncements = it }
        updateNotificationField("quietModeEnabled", false) { quietModeEnabled = it }
    }

    val enabledCount = listOf(dailyReminders, goalAchievements, taskUpdates, systemAnnouncements).count { it }
    val allNotificationsEnabled = enabledCount == 4

    Scaffold(containerColor = ScreenBg) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 16.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF2F6))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF5B6477)
                            )
                        }
                        Text(
                            text = "Notifications",
                            modifier = Modifier.align(Alignment.Center),
                            color = Deep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        IconButton(
                            onClick = { showActionsMenu = true },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF2F6))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = null,
                                tint = Color(0xFF5B6477)
                            )
                        }
                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (allNotificationsEnabled) "Turn all off" else "Turn all on")
                                },
                                onClick = {
                                    updateAllNotificationToggles(!allNotificationsEnabled)
                                    showActionsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reset defaults") },
                                onClick = {
                                    resetNotificationDefaults()
                                    showActionsMenu = false
                                }
                            )
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFEFE7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AccentOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (enabledCount > 0) "Status: Active" else "Status: Paused",
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "You have $enabledCount types of alerts enabled.",
                                    color = Color(0xFF7B8598),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "ALERT PREFERENCES",
                        color = Color(0xFFA0A8B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.1.sp
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    NotificationRow(
                        icon = Icons.Filled.CalendarMonth,
                        title = "Daily Reminders",
                        subtitle = "Morning summary of your tasks",
                        iconBoxColor = Color(0xFFFCE4CE),
                        checked = dailyReminders,
                        activeTrackColor = Color(0xFFF6C08E),
                        onCheckedChange = {
                            updateNotificationField(
                                field = "dailyReminders",
                                nextValue = it,
                                updateLocal = { value -> dailyReminders = value }
                            )
                        }
                    )
                }

                item {
                    NotificationRow(
                        icon = Icons.Filled.EmojiEvents,
                        title = "Goal Achievements",
                        subtitle = "Milestone celebration alerts",
                        iconBoxColor = Color(0xFFCDEEDB),
                        checked = goalAchievements,
                        activeTrackColor = Color(0xFFA7E4CA),
                        onCheckedChange = {
                            updateNotificationField(
                                field = "goalAchievements",
                                nextValue = it,
                                updateLocal = { value -> goalAchievements = value }
                            )
                        }
                    )
                }

                item {
                    NotificationRow(
                        icon = Icons.Filled.Checklist,
                        title = "Task Updates",
                        subtitle = "Notify when changes are made",
                        iconBoxColor = Color(0xFFFCE4CE),
                        checked = taskUpdates,
                        activeTrackColor = Color(0xFFF6C08E),
                        onCheckedChange = {
                            updateNotificationField(
                                field = "taskUpdates",
                                nextValue = it,
                                updateLocal = { value -> taskUpdates = value }
                            )
                        }
                    )
                }

                item {
                    NotificationRow(
                        icon = Icons.Filled.Campaign,
                        title = "System Announcements",
                        subtitle = "Important news and app updates",
                        iconBoxColor = Color(0xFFCDEEDB),
                        checked = systemAnnouncements,
                        activeTrackColor = Color(0xFFA7E4CA),
                        onCheckedChange = {
                            updateNotificationField(
                                field = "systemAnnouncements",
                                nextValue = it,
                                updateLocal = { value -> systemAnnouncements = value }
                            )
                        }
                    )
                }

                item {
                    Surface(
                        color = Color(0xFF121E3D),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Quiet Mode",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Disable all alerts from 10 PM",
                                    color = Color(0xFFA8B3CA),
                                    fontSize = 13.sp
                                )
                            }

                            Surface(
                                onClick = {
                                    val next = !quietModeEnabled
                                    updateNotificationField(
                                        field = "quietModeEnabled",
                                        nextValue = next,
                                        updateLocal = { value -> quietModeEnabled = value }
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = AccentOrange
                            ) {
                                Text(
                                    text = if (quietModeEnabled) "Disable" else "Enable",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBoxColor: Color,
    checked: Boolean,
    activeTrackColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = appCardSubtleColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBoxColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF42506A),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Deep,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = subtitle,
                    color = themedColor(Color(0xFF8C96A9), Color(0xFFAEB6C5)),
                    fontSize = 13.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = activeTrackColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFDEE4EE)
                )
            )
        }
    }
}
