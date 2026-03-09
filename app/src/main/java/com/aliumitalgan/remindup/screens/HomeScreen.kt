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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.core.di.RemindUpViewModelFactory
import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.presentation.home.HomeViewModel

private val SweetBackground = Color(0xFFF6F1EE)
private val DailyGoalCard = Color(0xFFE8DDF1)
private val MainTaskCard = Color(0xFFCBEEDD)
private val LilacCard = Color(0xFFE8DDF9)
private val SandCard = Color(0xFFF4E3C8)
private val NavyAction = Color(0xFF0A1C45)
private val AccentOrange = Color(0xFFF26522)

@Composable
fun HomeScreenContent(
    onNavigateToGoals: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAssistant: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = RemindUpViewModelFactory(LocalAppContainer.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val goals = uiState.goals.map { it.second }
    val reminders = uiState.reminders.map { it.reminder }
    val isLoading = uiState.isLoading

    val userName = uiState.userName

    val navItems = mainBottomNavItems()
    var currentRoute by remember { mutableStateOf("home") }

    val activeReminders = reminders.filter { it.isEnabled }
    val completedGoals = goals.count { it.progress >= 100 }
    val completionPercent = if (goals.isEmpty()) 0 else (completedGoals * 100 / goals.size)
    val spotlightTask = activeReminders.firstOrNull()
    val quickCards = activeReminders.drop(1).take(2)
    val upcoming = activeReminders.take(6)

    Scaffold(
        containerColor = SweetBackground,
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> Unit
                        "goals" -> onNavigateToGoals()
                        "social" -> onNavigateToSocial()
                        "analytic" -> onNavigateToProgress()
                        "settings" -> onNavigateToSettings()
                    }
                },
                onCenterActionClick = { onNavigateToGoals() }
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
                CircularProgressIndicator(color = AccentOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF9C5A3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hello, $userName",
                                fontSize = 13.sp,
                                color = Color(0xFF7F7F90)
                            )
                            Text(
                                text = "Good Morning!",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0A1C45),
                                lineHeight = 34.sp
                            )
                        }
                        IconButton(onClick = onNavigateToAssistant) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsNone,
                                contentDescription = "Alerts",
                                tint = AccentOrange
                            )
                        }
                    }
                }

                item {
                    Surface(
                        color = DailyGoalCard,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SweetTag("DAILY GOAL")
                                Text(
                                    text = "$completionPercent%",
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Almost there!",
                                fontSize = 30.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF15213B)
                            )
                            Text(
                                text = "You have completed $completedGoals of ${goals.size} goals today.",
                                color = Color(0xFF61627A),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color.White.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((completionPercent.coerceIn(0, 100)) / 100f)
                                        .height(8.dp)
                                        .background(AccentOrange)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                color = NavyAction,
                                shape = RoundedCornerShape(20.dp),
                                onClick = onNavigateToProgress
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("View Progress Stats", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    val title = spotlightTask?.title ?: "Morning Yoga"
                    val subtitle = spotlightTask?.description?.ifBlank { "Wellness" } ?: "Wellness"
                    Surface(
                        color = MainTaskCard,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToReminders
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.SelfImprovement, contentDescription = null, tint = Color(0xFF23A36A))
                                Text("Now", color = Color(0xFF15985E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF0F2B23))
                            Text(
                                text = "${spotlightTask?.time ?: "15 mins"} - $subtitle",
                                color = Color(0xFF3E7D66),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickTaskCard(
                            modifier = Modifier.weight(1f),
                            title = quickCards.getOrNull(0)?.title ?: "Drink Water",
                            subtitle = quickCards.getOrNull(0)?.description?.ifBlank { "2.5L / 3L Goal" } ?: "2.5L / 3L Goal",
                            bg = LilacCard,
                            icon = Icons.Filled.Check
                        )
                        QuickTaskCard(
                            modifier = Modifier.weight(1f),
                            title = quickCards.getOrNull(1)?.title ?: "Meal Prep",
                            subtitle = quickCards.getOrNull(1)?.time?.let { "$it PM Today" } ?: "12:30 PM Today",
                            bg = SandCard,
                            icon = Icons.Filled.Restaurant
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Upcoming", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color(0xFF141A34))
                        Text(
                            text = "View all",
                            color = AccentOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                items(upcoming.ifEmpty { listOf(Reminder(title = "Read 10 pages", time = "8:00 PM", description = "Every day")) }) { reminder ->
                    UpcomingTaskItem(
                        title = reminder.title.ifBlank { "Untitled Task" },
                        subtitle = reminder.description.ifBlank { "Every day at ${reminder.time}" },
                        icon = if (reminder.title.contains("read", true)) Icons.Filled.MenuBook else Icons.Filled.Check
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun SweetTag(text: String) {
    Surface(
        color = Color(0xFFFCECDD),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            color = AccentOrange,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun QuickTaskCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    bg: Color,
    icon: ImageVector
) {
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
            }
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF20233F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color(0xFF6D7191),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UpcomingTaskItem(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Surface(
        color = Color.White.copy(alpha = 0.94f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F5FB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF5A7AAE), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D233F))
                Text(subtitle, color = Color(0xFF7E869E), fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF4ED))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD5BF), Color(0xFFFFF4ED))
                        )
                    )
            )
        }
    }
}
