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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import com.google.firebase.auth.FirebaseAuth

private val AnalyticsBg = Color(0xFFF6F2F1)
private val Orange = Color(0xFFF26522)
private val DeepText = Color(0xFF1A1E3F)

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
    val avgProgress = if (goals.isEmpty()) 0 else goals.sumOf { it.progress } / goals.size
    val activePlans = reminders.count { it.isEnabled }
    val streakDays = goals.count { it.progress >= 50 } * 3
    val weeklyBars = buildWeeklyBars(goals)
    val badges = listOf(
        BadgeUi("Fast Finisher", Icons.Filled.Bolt, streakDays >= 7),
        BadgeUi("Deep Work", Icons.Filled.WorkspacePremium, avgProgress >= 60),
        BadgeUi("Self Care", Icons.Filled.FavoriteBorder, activePlans >= 3),
        BadgeUi("???", Icons.Filled.Lock, false)
    )

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
                },
                onCenterActionClick = onNavigateToGoals
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
                        color = Color.White,
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
                            Text("You completed $totalGoals tasks this week", color = Color(0xFF757C95), fontSize = 12.sp)
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
                        color = Color.White,
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
                    Text("Achievement Stickers", fontWeight = FontWeight.ExtraBold, color = DeepText, fontSize = 18.sp)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        badges.forEach { badge ->
                            BadgeItem(
                                badge = badge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 10.sp, color = Orange, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DeepText)
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF7B8098))
        }
    }
}

private data class BadgeUi(
    val title: String,
    val icon: ImageVector,
    val unlocked: Boolean
)

@Composable
private fun BadgeItem(
    badge: BadgeUi,
    modifier: Modifier = Modifier
) {
    val bg = if (badge.unlocked) Color(0xFFEAF1FF) else Color(0xFFEDEFF4)
    val tint = if (badge.unlocked) Orange else Color(0xFF9BA3B9)
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(badge.icon, contentDescription = null, tint = tint)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = badge.title,
                fontSize = 10.sp,
                color = if (badge.unlocked) DeepText else Color(0xFF9BA3B9),
                fontWeight = FontWeight.Bold
            )
        }
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
