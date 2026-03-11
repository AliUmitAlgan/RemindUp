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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.ui.theme.appCardColor
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import com.google.firebase.auth.FirebaseAuth

private val AnalyticsBg: Color
    get() = themedColor(Color(0xFFF6F2F1), Color(0xFF0F131A))
private val Orange = Color(0xFFF26522)
private val DeepText: Color
    get() = themedColor(Color(0xFF1A1E3F), Color(0xFFE5E7EB))

@Composable
fun ProgressScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {}
) {
    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentRoute by remember { mutableStateOf("analytic") }
    val navItems = mainBottomNavItems()

    LaunchedEffect(Unit) {
        isLoading = true
        goals = ProgressUtils.getUserGoals().getOrDefault(emptyList()).map { it.second }
        reminders = ReminderUtils.getUserReminders().getOrDefault(emptyList()).map { it.second }
        isLoading = false
    }

    val userName = FirebaseAuth.getInstance().currentUser?.displayName?.substringBefore(" ") ?: "Alex"
    val totalGoals = goals.size
    val activePlans = reminders.count { it.isEnabled }
    val streakDays = goals.count { it.progress >= 50 } * 3
    val weeklyBars = buildWeeklyBars(goals)
    val topHabits = buildTopHabits(goals, reminders)

    Scaffold(
        containerColor = AnalyticsBg,
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
                        "analytic" -> Unit
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = DeepText)
                        }
                        Text("RemindUp Sweet Analytics", fontWeight = FontWeight.Bold, color = DeepText)
                        IconButton(onClick = onNavigateToReminders) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = DeepText)
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = appCardColor,
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD7C4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Celebration, contentDescription = null, tint = Orange, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Great job, $userName!", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = DeepText)
                            Text(
                                "You completed $totalGoals tasks this week",
                                color = themedColor(Color(0xFF757C95), Color(0xFFAEB6C5)),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("CURRENT STREAK", "$streakDays Days", "Next Milestone 15 Days", Modifier.weight(1f))
                        StatCard("Total Wins", "$totalGoals", "avg +12%", Modifier.weight(1f))
                        StatCard("Focus Hours", "${activePlans * 2}", "avg +8%", Modifier.weight(1f))
                    }
                }

                item {
                    Surface(
                        color = appCardColor,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Weekly Progress", fontWeight = FontWeight.ExtraBold, color = DeepText)
                                Text("SUGAR LEVEL HIGH", color = Orange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklyBars.forEachIndexed { index, value ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height((20 + value * 56).dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (index == 3) Orange else Color(0xFFCFD6E7))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(dayLabel(index), fontSize = 10.sp, color = Color(0xFF7E84A1))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Top Habits", fontWeight = FontWeight.ExtraBold, color = DeepText, fontSize = 18.sp)
                        Text("View all", color = Orange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                items(topHabits) { habit ->
                    TopHabitCard(habit = habit)
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = appCardColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 10.sp, color = Orange, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DeepText)
            Text(subtitle, fontSize = 10.sp, color = themedColor(Color(0xFF7B8098), Color(0xFFAEB6C5)))
        }
    }
}

private data class HabitUi(
    val title: String,
    val subtitle: String,
    val progress: Int,
    val streakDays: Int,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val progressColor: Color
)

@Composable
private fun TopHabitCard(habit: HabitUi) {
    Surface(
        color = appCardColor,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(habit.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = habit.icon,
                    contentDescription = null,
                    tint = habit.iconTint,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    color = DeepText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                )
                Text(
                    text = habit.subtitle,
                    color = themedColor(Color(0xFF6F7B95), Color(0xFFAEB6C5)),
                    fontSize = 14.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${habit.progress}%",
                    color = habit.progressColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = "STREAK: ${habit.streakDays}D",
                    color = themedColor(Color(0xFF92A0B8), Color(0xFF9FB0C7)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun buildTopHabits(goals: List<Goal>, reminders: List<Reminder>): List<HabitUi> {
    val colorSet = listOf(
        Triple(Color(0xFF24A69A), Color(0xFFDDEDEB), Icons.Filled.WaterDrop),
        Triple(Color(0xFFF07A22), Color(0xFFF5E8D3), Icons.Filled.SelfImprovement),
        Triple(Color(0xFF4A83F6), Color(0xFFDDE6F7), Icons.Filled.NightlightRound)
    )

    val fromGoals = goals
        .sortedByDescending { it.progress }
        .take(3)
        .mapIndexed { index, goal ->
            val palette = colorSet[index % colorSet.size]
            HabitUi(
                title = goal.title.ifBlank { defaultHabitTitle(index) },
                subtitle = goal.description.ifBlank { defaultHabitSubtitle(index) },
                progress = goal.progress.coerceIn(0, 100),
                streakDays = (goal.progress.coerceIn(0, 100) / 8).coerceAtLeast(1),
                icon = palette.third,
                iconTint = palette.first,
                iconBg = palette.second,
                progressColor = palette.first
            )
        }

    if (fromGoals.size == 3) return fromGoals

    val fromReminders = reminders
        .filter { it.isEnabled }
        .take(3)
        .mapIndexed { index, reminder ->
            val palette = colorSet[index % colorSet.size]
            HabitUi(
                title = reminder.title.ifBlank { defaultHabitTitle(index) },
                subtitle = reminder.description.ifBlank { defaultHabitSubtitle(index) },
                progress = listOf(100, 92, 78)[index % 3],
                streakDays = listOf(12, 5, 3)[index % 3],
                icon = palette.third,
                iconTint = palette.first,
                iconBg = palette.second,
                progressColor = palette.first
            )
        }

    val fallback = listOf(
        HabitUi(
            title = "Drink Water",
            subtitle = "8 glasses daily",
            progress = 100,
            streakDays = 12,
            icon = Icons.Filled.WaterDrop,
            iconTint = Color(0xFF24A69A),
            iconBg = Color(0xFFDDEDEB),
            progressColor = Color(0xFF24A69A)
        ),
        HabitUi(
            title = "Morning Meditation",
            subtitle = "15 minutes daily",
            progress = 92,
            streakDays = 5,
            icon = Icons.Filled.SelfImprovement,
            iconTint = Color(0xFFF07A22),
            iconBg = Color(0xFFF5E8D3),
            progressColor = Color(0xFFF07A22)
        ),
        HabitUi(
            title = "Sleep Early",
            subtitle = "Before 11:00 PM",
            progress = 78,
            streakDays = 3,
            icon = Icons.Filled.NightlightRound,
            iconTint = Color(0xFF4A83F6),
            iconBg = Color(0xFFDDE6F7),
            progressColor = Color(0xFF4A83F6)
        )
    )

    return (fromGoals + fromReminders + fallback)
        .distinctBy { it.title }
        .take(3)
}

private fun defaultHabitTitle(index: Int): String {
    return when (index) {
        0 -> "Drink Water"
        1 -> "Morning Meditation"
        else -> "Sleep Early"
    }
}

private fun defaultHabitSubtitle(index: Int): String {
    return when (index) {
        0 -> "8 glasses daily"
        1 -> "15 minutes daily"
        else -> "Before 11:00 PM"
    }
}

private fun buildWeeklyBars(goals: List<Goal>): List<Float> {
    if (goals.isEmpty()) return listOf(0.35f, 0.45f, 0.38f, 0.66f, 0.22f, 0.25f, 0.24f)
    val sum = goals.sumOf { it.progress }
    return List(7) { index ->
        (((sum + (index * 17)) % 100) / 100f).coerceIn(0.18f, 0.9f)
    }
}

private fun dayLabel(index: Int): String {
    return when (index) {
        0 -> "Mon"
        1 -> "Tue"
        2 -> "Wed"
        3 -> "Thu"
        4 -> "Fri"
        5 -> "Sat"
        else -> "Sun"
    }
}

