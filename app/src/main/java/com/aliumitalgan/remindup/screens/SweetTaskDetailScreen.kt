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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.DeleteGoalDialog
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.presentation.goals.GoalsViewModel
import com.aliumitalgan.remindup.core.di.RemindUpViewModelFactory
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.ReminderUtils

private val DetailBackground = Color(0xFFF8F3EF)
private val SweetOrange = Color(0xFFF26522)
private val DeepText = Color(0xFF1A2140)
private val SoftText = Color(0xFF7B829D)

@Composable
fun SweetTaskDetailScreen(
    goalId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {},
    viewModel: GoalsViewModel = viewModel(
        factory = RemindUpViewModelFactory(LocalAppContainer.current)
    )
) {
    var goalEntry by remember { mutableStateOf<Pair<String, Goal>?>(null) }
    var relatedReminders by remember { mutableStateOf<List<Pair<String, Reminder>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentRoute by remember { mutableStateOf("goals") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val navItems = mainBottomNavItems()

    LaunchedEffect(goalId) {
        isLoading = true
        val goals = ProgressUtils.getUserGoals().getOrDefault(emptyList())
        val reminders = ReminderUtils.getUserReminders().getOrDefault(emptyList())
        goalEntry = goals.firstOrNull { it.first == goalId }
        relatedReminders = resolveRelatedReminders(goalEntry?.second, reminders)
        isLoading = false
    }

    Scaffold(
        containerColor = DetailBackground,
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
                        "analytic" -> onNavigateToProgress()
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
                CircularProgressIndicator(color = SweetOrange)
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
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = DeepText)
                        }
                        Text(
                            text = "Sweet Task Detail",
                            color = DeepText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                        }
                    }
                }

                if (goalEntry == null) {
                    item {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Goal not found.",
                                color = DeepText,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    item {
                        GoalDetailCard(goal = goalEntry!!.second)
                    }

                    item {
                        Text(
                            text = "Reminder Touch",
                            color = DeepText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }

                    if (relatedReminders.isEmpty()) {
                        item {
                            Surface(
                                color = Color(0xFFFFF4EC),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.NotificationsActive,
                                        contentDescription = null,
                                        tint = SweetOrange
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "This goal has no matching reminder yet.",
                                        color = SoftText
                                    )
                                }
                            }
                        }
                    } else {
                        items(relatedReminders, key = { it.first }) { (_, reminder) ->
                            ReminderDetailCard(reminder = reminder)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteGoalDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirmDelete = {
                viewModel.deleteGoal(goalId)
                showDeleteDialog = false
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun GoalDetailCard(goal: Goal) {
    val accent = when (goalCategoryName(goal.category)) {
        "Health" -> Color(0xFF5C8EE6)
        "Learning" -> Color(0xFFA57CF0)
        "Work" -> Color(0xFF72CFAF)
        else -> SweetOrange
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(goalCategoryName(goal.category), color = accent, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("${goal.progress}%", color = accent, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = goal.title.ifBlank { "Untitled Goal" },
                color = DeepText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = goal.description,
                    color = SoftText,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFF2F3F8),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.progress.coerceIn(0, 100) / 100f)
                        .height(8.dp)
                        .background(accent)
                )
            }
        }
    }
}

@Composable
private fun ReminderDetailCard(reminder: Reminder) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Alarm, contentDescription = null, tint = SweetOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(reminder.time.ifBlank { "--:--" }, color = SweetOrange, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                if (reminder.isImportant) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = reminder.title.ifBlank { "Untitled Reminder" },
                color = DeepText,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = reminder.description.ifBlank { "No extra note." },
                color = SoftText,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun resolveRelatedReminders(
    goal: Goal?,
    reminders: List<Pair<String, Reminder>>
): List<Pair<String, Reminder>> {
    if (goal == null) return emptyList()
    if (reminders.isEmpty()) return emptyList()

    val keywordTokens = (goal.title + " " + goal.description)
        .lowercase()
        .split(Regex("[^a-z0-9çğıöşü]+"))
        .filter { it.length > 2 }
        .toSet()

    val bySimilarity = reminders.filter { (_, reminder) ->
        val reminderText = (reminder.title + " " + reminder.description).lowercase()
        val hasKeywordMatch = keywordTokens.any { token -> reminderText.contains(token) }
        hasKeywordMatch || categoryMatches(goal.category, reminder.category)
    }

    return bySimilarity
        .ifEmpty { reminders.filter { it.second.isEnabled } }
        .ifEmpty { reminders }
        .take(3)
}

private fun categoryMatches(goalCategory: Int, reminderCategory: ReminderCategory): Boolean {
    return when (goalCategoryName(goalCategory)) {
        "Health" -> reminderCategory == ReminderCategory.HEALTH || reminderCategory == ReminderCategory.FITNESS
        "Learning" -> reminderCategory == ReminderCategory.STUDY || reminderCategory == ReminderCategory.PERSONAL
        "Work" -> reminderCategory == ReminderCategory.WORK
        else -> reminderCategory == ReminderCategory.GENERAL
    }
}

private fun goalCategoryName(category: Int): String {
    return when (category) {
        1 -> "Health"
        2 -> "Learning"
        3 -> "Work"
        else -> "Health"
    }
}
