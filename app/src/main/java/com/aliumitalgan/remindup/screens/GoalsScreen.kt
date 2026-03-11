package com.aliumitalgan.remindup.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.DeleteGoalDialog
import com.aliumitalgan.remindup.components.EmptyGoalsView
import com.aliumitalgan.remindup.components.NoGoalsFoundView
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.core.di.RemindUpViewModelFactory
import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.presentation.goals.GoalsViewModel
import com.aliumitalgan.remindup.ui.theme.appTextPrimary
import com.aliumitalgan.remindup.ui.theme.appTextSecondary
import com.aliumitalgan.remindup.ui.theme.themedColor
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val GoalsBackground: Color
    get() = themedColor(Color(0xFFF4F3F3), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val AccentPurple = Color(0xFFA66CF1)
private val AccentMint = Color(0xFF2BBF8A)
private val AccentGreen = Color(0xFF22C55E)
private val GoalsHeaderSurface: Color
    get() = themedColor(Color(0xFFFBFBFC), Color(0xFF171D26))
private val GoalsChipContainer: Color
    get() = themedColor(Color(0xFFEFF2F6), Color(0xFF243041))
private val GoalsCardSurface: Color
    get() = themedColor(Color(0xFFFAFAFB), Color(0xFF171D26))
private val GoalsWarmCard: Color
    get() = themedColor(Color(0xFFF9E7DC), Color(0xFF241B16))
private val GoalsWarmCardText: Color
    get() = themedColor(Color(0xFF8B6F63), Color(0xFFC7B0A3))
private val GoalsTrackColor: Color
    get() = themedColor(Color(0xFFE5E7EB), Color(0xFF2A3542))
private val GoalsSearchField: Color
    get() = themedColor(Color(0xFFF3F4F6), Color(0xFF222C3B))
private val GoalsIconButton: Color
    get() = themedColor(Color(0xFFF2F4F7), Color(0xFF232F40))

private data class GoalFilterOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val accent: Color
)

private val goalFilterOptions = listOf(
    GoalFilterOption("All Goals", "All", Icons.Filled.AutoAwesome, Color(0xFF64748B)),
    GoalFilterOption("Health", "Health", Icons.Filled.LocalFlorist, AccentOrange),
    GoalFilterOption("Learning", "Learning", Icons.Filled.School, AccentPurple),
    GoalFilterOption("Work", "Work", Icons.Filled.Work, AccentMint),
    GoalFilterOption("Personal", "Personal", Icons.Filled.Star, AccentGreen),
    GoalFilterOption("Hobby", "Hobby", Icons.Filled.Brush, Color(0xFF3B82F6))
)

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
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var smartRemindersEnabled by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("goals") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var pendingDeleteGoalId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val dateLabelFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val displayTimeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
    val storageTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val navItems = mainBottomNavItems()
    val filteredGoals = goals.filter { (_, goal) ->
        selectedFilter == "All Goals" || mapCategory(goal.category) == selectedFilter
    }
    val visibleGoals = filteredGoals.filter { (_, goal) ->
        val query = searchQuery.trim()
        query.isBlank() ||
            goal.title.contains(query, ignoreCase = true) ||
            goal.description.contains(query, ignoreCase = true) ||
            mapCategory(goal.category).contains(query, ignoreCase = true)
    }
    val headerTitle = if (selectedFilter == "All Goals") "All Goals" else "$selectedFilter Goals"
    val addGoalLabel = if (selectedFilter == "All Goals") "Add New Goal" else "Add $selectedFilter Goal"

    Scaffold(
        containerColor = GoalsBackground,
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
                    Surface(
                        color = GoalsHeaderSurface,
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 2.dp,
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = headerTitle,
                                    color = appTextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HeaderCircleButton(
                                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        onClick = onNavigateBack
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    HeaderCircleButton(
                                        icon = Icons.Filled.Search,
                                        contentDescription = "Search goals",
                                        onClick = {
                                            if (isSearchVisible && searchQuery.isNotBlank()) {
                                                searchQuery = ""
                                            } else {
                                                isSearchVisible = !isSearchVisible
                                                if (!isSearchVisible) {
                                                    searchQuery = ""
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            if (isSearchVisible) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            text = "Search goals",
                                            color = appTextSecondary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = appTextSecondary
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotBlank()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Clear search",
                                                    tint = appTextSecondary
                                                )
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = GoalsSearchField,
                                        unfocusedContainerColor = GoalsSearchField,
                                        disabledContainerColor = GoalsSearchField,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedTextColor = appTextPrimary,
                                        unfocusedTextColor = appTextPrimary,
                                        cursorColor = AccentOrange
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                goalFilterOptions.forEach { filter ->
                                    GoalFilterChip(
                                        option = filter,
                                        selected = selectedFilter == filter.key,
                                        onClick = { viewModel.setFilter(filter.key) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (goals.isEmpty()) {
                    item {
                        EmptyGoalsView(
                            onCreateGoal = {
                                categoryInput = defaultCategoryForFilter(selectedFilter, categoryInput)
                                showAddDialog = true
                            }
                        )
                    }
                } else if (visibleGoals.isEmpty()) {
                    item {
                        NoGoalsFoundView(
                            onClearFilters = {
                                viewModel.setFilter("All Goals")
                                searchQuery = ""
                                isSearchVisible = false
                            }
                        )
                    }
                } else {
                    items(visibleGoals, key = { it.first }) { (goalId, goal) ->
                        GoalOverviewCard(
                            goal = goal,
                            onClick = { onNavigateToSweetTaskDetail(goalId) },
                            onDeleteClick = { pendingDeleteGoalId = goalId }
                        )
                    }
                }

                item {
                    AddGoalInlineButton(
                        label = addGoalLabel,
                        onClick = {
                            categoryInput = defaultCategoryForFilter(selectedFilter, categoryInput)
                            showAddDialog = true
                        }
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoalsWarmCard,
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
                                    color = GoalsWarmCardText,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(themedColor(Color.White, Color(0xFF2A3548))),
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
            dateLabel = selectedDate.format(dateLabelFormatter),
            timeLabel = selectedTime.format(displayTimeFormatter),
            smartRemindersEnabled = smartRemindersEnabled,
            isSaving = isSaving,
            onDismiss = {
                showAddDialog = false
                isSaving = false
            },
            onTitleChange = { titleInput = it },
            onCategorySelected = { categoryInput = it },
            onDateClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    },
                    selectedDate.year,
                    selectedDate.monthValue - 1,
                    selectedDate.dayOfMonth
                ).apply {
                    datePicker.minDate = LocalDate.now()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                }.show()
            },
            onTimeClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedTime = LocalTime.of(hourOfDay, minute)
                    },
                    selectedTime.hour,
                    selectedTime.minute,
                    false
                ).show()
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
                    dueDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    reminderTime = selectedTime.format(storageTimeFormatter),
                    smartReminderEnabled = smartRemindersEnabled
                )
                titleInput = ""
                categoryInput = defaultCategoryForFilter(selectedFilter, "Health")
                selectedDate = LocalDate.now()
                selectedTime = LocalTime.of(8, 0)
                smartRemindersEnabled = true
                showAddDialog = false
                isSaving = false
            }
        )
    }

    pendingDeleteGoalId?.let { goalId ->
        DeleteGoalDialog(
            onDismiss = { pendingDeleteGoalId = null },
            onConfirmDelete = {
                viewModel.deleteGoal(goalId)
                pendingDeleteGoalId = null
            }
        )
    }
}

@Composable
private fun HeaderCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = GoalsIconButton,
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = appTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GoalFilterChip(
    option: GoalFilterOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) AccentOrange else GoalsChipContainer,
        modifier = Modifier.heightIn(min = 44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (selected) Color.White else option.accent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = option.label,
                color = if (selected) Color.White else themedColor(Color(0xFF334155), Color(0xFFD5DCE7)),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AddGoalInlineButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentOrange,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GoalOverviewCard(
    goal: Goal,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val accent = when (mapCategory(goal.category)) {
        "Health" -> AccentOrange
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

    Surface(
        color = GoalsCardSurface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title.ifBlank { "Untitled Goal" },
                        color = appTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitleFor(goal),
                        color = appTextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box {
                    IconButton(
                        onClick = { isMenuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Goal actions",
                            tint = themedColor(Color(0xFF94A3B8), Color(0xFFAEB6C5))
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Goal") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = AccentOrange
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progressLabelFor(goal),
                    color = themedColor(Color(0xFF6B7280), Color(0xFFAEB6C5)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = progressValueFor(goal),
                    color = themedColor(Color(0xFF64748B), Color(0xFFD5DCE7)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = GoalsTrackColor,
                shape = RoundedCornerShape(999.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.progress.coerceIn(0, 100) / 100f)
                        .height(10.dp)
                        .background(accent)
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
    val dialogContainer = themedColor(Color.White, Color(0xFF161D27))
    val heading = themedColor(Color(0xFF2B3342), Color(0xFFE5E7EB))
    val labelColor = themedColor(Color(0xFF7C8495), Color(0xFF9AA6B2))
    val fieldBackground = themedColor(Color(0xFFF9FAFB), Color(0xFF222C3B))
    val fieldText = themedColor(Color(0xFF313B4B), Color(0xFFE5E7EB))
    val closeBg = themedColor(Color(0xFFF3F4F6), Color(0xFF2A3548))
    val closeTint = themedColor(Color(0xFFB9C0CA), Color(0xFF94A3B8))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(40.dp),
            color = dialogContainer,
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
                        color = heading,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(closeBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = closeTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Goal Title",
                        color = labelColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    TextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text(
                                text = "e.g. Morning Yoga",
                                color = labelColor.copy(alpha = 0.7f),
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
                            focusedContainerColor = fieldBackground,
                            unfocusedContainerColor = fieldBackground,
                            disabledContainerColor = fieldBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = AccentOrange,
                            focusedTextColor = fieldText,
                            unfocusedTextColor = fieldText
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Category",
                        color = labelColor,
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
                            icon = Icons.Filled.Work,
                            label = "Work",
                            selected = selectedCategory == "Work",
                            selectedColor = Color(0xFFDFF7EE),
                            textColor = Color(0xFF0F9D73),
                            borderColor = Color(0xFFB9ECD7),
                            onClick = { onCategorySelected("Work") }
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
                            color = labelColor,
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
                            color = labelColor,
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
                            .background(themedColor(Color(0xFFFFEDD5), Color(0xFF2A3548))),
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
                            color = heading,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Gently nudge me",
                            color = labelColor,
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
                            uncheckedTrackColor = themedColor(Color(0xFFE5E7EB), Color(0xFF475569)),
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
        color = if (selected) selectedColor else themedColor(Color(0xFFEFF2F7), Color(0xFF253143)),
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
        color = themedColor(Color(0xFFF9FAFB), Color(0xFF222C3B)),
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
                color = themedColor(Color(0xFF4B5563), Color(0xFFE5E7EB)),
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

private fun defaultCategoryForFilter(selectedFilter: String, fallback: String): String {
    return when (selectedFilter) {
        "Health", "Learning", "Work", "Personal", "Hobby" -> selectedFilter
        else -> fallback
    }
}

private fun subtitleFor(goal: Goal): String {
    val cadence = when (mapCategory(goal.category)) {
        "Health" -> "Daily routine"
        "Learning" -> "Practice session"
        "Work" -> "Focus session"
        "Personal" -> "Personal check-in"
        "Hobby" -> "Creative session"
        else -> "Daily goal"
    }
    val time = formatGoalTime(goal.reminderTime)
    return if (time != null) "$cadence - $time" else cadence
}

private fun progressLabelFor(goal: Goal): String {
    return when (mapCategory(goal.category)) {
        "Health", "Personal", "Hobby" -> "Weekly Progress"
        else -> "Daily Progress"
    }
}

private fun progressValueFor(goal: Goal): String {
    val completed = ((goal.progress.coerceIn(0, 100) / 20f).roundToInt()).coerceIn(0, 5)
    return when (mapCategory(goal.category)) {
        "Health", "Personal", "Hobby" -> "$completed/5 days"
        "Learning" -> "$completed/5 sessions"
        "Work" -> "$completed/5 tasks"
        else -> "${goal.progress}%"
    }
}

private fun formatGoalTime(value: String): String? {
    val raw = value.trim()
    if (raw.isEmpty()) {
        return null
    }

    val patterns = listOf("HH:mm", "hh:mm a")
    for (pattern in patterns) {
        val parsed = runCatching {
            LocalTime.parse(raw, DateTimeFormatter.ofPattern(pattern))
        }.getOrNull()
        if (parsed != null) {
            return parsed.format(DateTimeFormatter.ofPattern("hh:mm a"))
        }
    }
    return null
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
