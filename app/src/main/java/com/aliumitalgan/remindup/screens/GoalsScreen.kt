package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.EmptyGoalsView
import com.aliumitalgan.remindup.components.NoGoalsFoundView
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.core.di.RemindUpViewModelFactory
import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.presentation.goals.GoalsViewModel
import com.aliumitalgan.remindup.ui.theme.appCardColor
import com.aliumitalgan.remindup.ui.theme.themedColor
import java.util.Locale

private val GoalsBackground: Color
    get() = themedColor(Color(0xFFF4F3F3), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val AccentBlue = Color(0xFF5B8DE8)
private val AccentPurple = Color(0xFFA66CF1)
private val AccentMint = Color(0xFF2BBF8A)
private val AccentGreen = Color(0xFF22C55E)

@Composable
fun GoalsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {},
    onNavigateToSweetTaskDetail: (String) -> Unit = {},
    viewModel: GoalsViewModel = viewModel(
        factory = RemindUpViewModelFactory(LocalAppContainer.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val goals = uiState.goals
    val isLoading = uiState.isLoading
    val selectedFilter = uiState.selectedFilter

    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Health") }
    var dateInput by remember { mutableStateOf("Today") }
    var timeInput by remember { mutableStateOf("08:00 AM") }
    var smartRemindersEnabled by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("goals") }

    val navItems = mainBottomNavItems()
    val filteredGoals = goals.filter { (_, goal) ->
        when (selectedFilter) {
            "Health" -> mapCategory(goal.category) == "Health"
            "Learning" -> mapCategory(goal.category) == "Learning"
            "Work" -> mapCategory(goal.category) == "Work"
            else -> true
        }
    }

    Scaffold(
        containerColor = GoalsBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentOrange,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add goal")
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
                        "goals" -> Unit
                        "social" -> onNavigateToSocial()
                        "analytic" -> onNavigateToProgress()
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
                CircularProgressIndicator(color = AccentOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFDFC9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Goal",
                            color = Color(0xFF1F2B46),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = Color(0xFF2E3B56)
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All Goals", "Health", "Learning", "Work").forEach { filter ->
                            val selected = selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setFilter(filter) },
                                label = {
                                    Text(
                                        text = filter,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (filter) {
                                        "All Goals" -> AccentOrange
                                        "Health" -> AccentBlue.copy(alpha = 0.18f)
                                        "Learning" -> AccentPurple.copy(alpha = 0.2f)
                                        else -> AccentMint.copy(alpha = 0.2f)
                                    },
                                    selectedLabelColor = if (filter == "All Goals") Color.White else Color(0xFF23304D),
                                    containerColor = Color(0xFFEFEFF2)
                                )
                            )
                        }
                    }
                }

                if (goals.isNotEmpty()) {
                    item {
                        Text(
                            text = "My Sweet Progress",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp,
                            color = Color(0xFF1A2140)
                        )
                        Text(
                            text = "You have ${filteredGoals.size} goals active today!",
                            color = Color(0xFF7D86A0),
                            fontSize = 13.sp
                        )
                    }
                }

                if (goals.isEmpty()) {
                    item {
                        EmptyGoalsView(onCreateGoal = { showAddDialog = true })
                    }
                } else if (filteredGoals.isEmpty()) {
                    item {
                        NoGoalsFoundView(onClearFilters = { viewModel.setFilter("All Goals") })
                    }
                } else {
                    items(filteredGoals) { (goalId, goal) ->
                        GoalOverviewCard(
                            goal = goal,
                            onClick = { onNavigateToSweetTaskDetail(goalId) }
                        )
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF9E7DC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unlock Premium Gems", color = AccentOrange, fontWeight = FontWeight.ExtraBold)
                                Text(
                                    "Get 3D animated icons and advanced analytics for your goals.",
                                    color = Color(0xFF8B6F63),
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = AccentOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateGoalDialog(
            title = titleInput,
            selectedCategory = categoryInput,
            dateLabel = dateInput,
            timeLabel = timeInput,
            smartRemindersEnabled = smartRemindersEnabled,
            isSaving = isSaving,
            onDismiss = {
                showAddDialog = false
                isSaving = false
            },
            onTitleChange = { titleInput = it },
            onCategorySelected = { categoryInput = it },
            onDateClick = {
                dateInput = if (dateInput == "Today") "Tomorrow" else "Today"
            },
            onTimeClick = {
                timeInput = if (timeInput == "08:00 AM") "07:30 PM" else "08:00 AM"
            },
            onSmartReminderToggle = { smartRemindersEnabled = it },
            onCreateGoal = {
                isSaving = true
                val category = when (categoryInput.trim().lowercase(Locale.getDefault())) {
                    "learning" -> 2
                    "work" -> 3
                    "personal" -> 4
                    "hobby" -> 5
                    else -> 1
                }
                viewModel.addGoal(
                    title = titleInput.trim(),
                    description = "",
                    category = category,
                    dueDate = dateInput,
                    reminderTime = timeInput,
                    smartReminderEnabled = smartRemindersEnabled
                )
                titleInput = ""
                categoryInput = "Health"
                dateInput = "Today"
                timeInput = "08:00 AM"
                smartRemindersEnabled = true
                showAddDialog = false
                isSaving = false
            }
        )
    }
}

@Composable
private fun GoalOverviewCard(
    goal: Goal,
    onClick: () -> Unit
) {
    val accent = when (mapCategory(goal.category)) {
        "Health" -> AccentBlue
        "Learning" -> AccentPurple
        "Work" -> AccentMint
        "Personal" -> AccentGreen
        "Hobby" -> Color(0xFF3B82F6)
        else -> Color(0xFF9BA3B8)
    }
    val icon = when (mapCategory(goal.category)) {
        "Health" -> Icons.Filled.LocalFlorist
        "Learning" -> Icons.Filled.School
        "Work" -> Icons.Filled.Work
        "Personal" -> Icons.Filled.Star
        "Hobby" -> Icons.Filled.Brush
        else -> Icons.Filled.Language
    }
    val progressRatio = goal.progress.coerceIn(0, 100) / 100f

    Surface(
        color = appCardColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mapCategory(goal.category).uppercase(Locale.getDefault()),
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = goal.title.ifBlank { "Untitled Goal" },
                        color = themedColor(Color(0xFF232D44), Color(0xFFE5E7EB)),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${goal.progress}%",
                        color = accent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp),
                color = Color(0xFFE7EBF3),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressRatio)
                        .height(9.dp)
                        .background(accent)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progressSummary(goal.progress),
                    color = themedColor(Color(0xFF7B849A), Color(0xFFAEB6C5)),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = actionLabelFor(goal),
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CreateGoalDialog(
    title: String,
    selectedCategory: String,
    dateLabel: String,
    timeLabel: String,
    smartRemindersEnabled: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onSmartReminderToggle: (Boolean) -> Unit,
    onCreateGoal: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(40.dp),
            color = Color.White,
            shadowElevation = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Goal",
                        color = Color(0xFF2B3342),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFB9C0CA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Goal Title",
                        color = Color(0xFF7C8495),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    TextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text(
                                text = "e.g. Morning Yoga",
                                color = Color(0xFFD1D5DB),
                                fontWeight = FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFF2C34B),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            disabledContainerColor = Color(0xFFF9FAFB),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = AccentOrange,
                            focusedTextColor = Color(0xFF313B4B),
                            unfocusedTextColor = Color(0xFF313B4B)
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Category",
                        color = Color(0xFF7C8495),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalCategoryChip(
                            icon = Icons.Filled.LocalFlorist,
                            label = "Health",
                            selected = selectedCategory == "Health",
                            selectedColor = Color(0xFFD1FAE5),
                            textColor = Color(0xFF047857),
                            borderColor = Color(0xFFA7F3D0),
                            onClick = { onCategorySelected("Health") }
                        )
                        GoalCategoryChip(
                            icon = Icons.Filled.School,
                            label = "Learning",
                            selected = selectedCategory == "Learning",
                            selectedColor = Color(0xFFEDE9FE),
                            textColor = Color(0xFF7C3AED),
                            borderColor = Color(0xFFDDD6FE),
                            onClick = { onCategorySelected("Learning") }
                        )
                        GoalCategoryChip(
                            icon = Icons.Filled.Star,
                            label = "Personal",
                            selected = selectedCategory == "Personal",
                            selectedColor = Color(0xFFDCFCE7),
                            textColor = Color(0xFF15803D),
                            borderColor = Color(0xFFBBF7D0),
                            onClick = { onCategorySelected("Personal") }
                        )
                        GoalCategoryChip(
                            icon = Icons.Filled.Brush,
                            label = "Hobby",
                            selected = selectedCategory == "Hobby",
                            selectedColor = Color(0xFFEFF6FF),
                            textColor = Color(0xFF2563EB),
                            borderColor = Color(0xFFBFDBFE),
                            onClick = { onCategorySelected("Hobby") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Date",
                            color = Color(0xFF7C8495),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        SoftSelectorField(
                            value = dateLabel,
                            icon = Icons.Filled.CalendarMonth,
                            iconTint = Color(0xFFEF4444),
                            onClick = onDateClick
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Time",
                            color = Color(0xFF7C8495),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        SoftSelectorField(
                            value = timeLabel,
                            icon = Icons.Filled.AccessTime,
                            iconTint = Color(0xFFEF4444),
                            onClick = onTimeClick
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEDD5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Reminders",
                            color = Color(0xFF2F3A4E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Gently nudge me",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = smartRemindersEnabled,
                        onCheckedChange = onSmartReminderToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF68B5C),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE5E7EB),
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                Button(
                    onClick = onCreateGoal,
                    enabled = title.isNotBlank() && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF68B5C))
                ) {
                    Text(
                        text = if (isSaving) "Creating..." else "Create Goal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCategoryChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    selectedColor: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100),
        color = if (selected) selectedColor else Color(0xFFEFF2F7),
        border = BorderStroke(2.dp, if (selected) borderColor else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SoftSelectorField(
    value: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF9FAFB),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = Color(0xFF4B5563),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun progressSummary(progress: Int): String {
    val current = (progress.coerceIn(0, 100) / 10).coerceAtLeast(1)
    return "$current of 10 days streak"
}

private fun actionLabelFor(goal: Goal): String {
    return when (mapCategory(goal.category)) {
        "Health" -> "Complete"
        "Learning" -> "Practice"
        "Personal" -> "Reflect"
        "Hobby" -> "Create"
        else -> "Add Log"
    }
}

private fun mapCategory(category: Int): String {
    return when (category) {
        1 -> "Health"
        2 -> "Learning"
        3 -> "Work"
        4 -> "Personal"
        5 -> "Hobby"
        else -> "Health"
    }
}
