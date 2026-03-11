package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.core.di.RemindUpViewModelFactory
import com.aliumitalgan.remindup.presentation.goals.GoalsViewModel
import com.aliumitalgan.remindup.ui.theme.appCardColor
import com.aliumitalgan.remindup.ui.theme.appTextPrimary
import com.aliumitalgan.remindup.ui.theme.appTextSecondary
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.aliumitalgan.remindup.utils.SubGoal
import com.aliumitalgan.remindup.utils.SubGoalUtils
import java.util.UUID
import kotlinx.coroutines.launch

private val TaskBg: Color
    get() = themedColor(Color(0xFFFDF8F6), Color(0xFF0F131A))
private val PrimaryOrange = Color(0xFFEC5B13)
private val SoftText: Color
    get() = themedColor(Color(0xFF5A4A42), Color(0xFFE5E7EB))
private val LightCard: Color
    get() = appCardColor
private val Peach: Color
    get() = themedColor(Color(0xFFFFEDDF), Color(0xFF2A3548))
private val Mint: Color
    get() = themedColor(Color(0xFFE0F2F1), Color(0xFF1E3A36))
private val Lavender: Color
    get() = themedColor(Color(0xFFF3E8FF), Color(0xFF2A233B))
private val TaskMutedText: Color
    get() = themedColor(Color(0xFF9AA6B2), Color(0xFF94A3B8))
private val TaskInputBg: Color
    get() = themedColor(Color(0xFFF1F4F8), Color(0xFF222C3B))
private val TaskSoftBorder: Color
    get() = themedColor(Color(0xFFFFE6D7), Color(0xFF3B475A))

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
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val goalEntry = uiState.goals.firstOrNull { it.first == goalId }
    val goalName = goalEntry?.second?.title?.ifBlank { "Goal" } ?: "Goal"
    val scope = rememberCoroutineScope()

    var tasks by remember { mutableStateOf<List<Pair<String, SubGoal>>>(emptyList()) }
    var isTasksLoading by remember { mutableStateOf(true) }
    var currentRoute by remember { mutableStateOf("goals") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    val navItems = mainBottomNavItems()
    val completedTasks = tasks.count { it.second.completed }
    val totalTasks = tasks.size.coerceAtLeast(1)
    val progress = if (tasks.isNotEmpty()) {
        (completedTasks * 100 / totalTasks).coerceIn(0, 100)
    } else {
        goalEntry?.second?.progress?.coerceIn(0, 100) ?: 0
    }
    val remainingTasks = (totalTasks - completedTasks).coerceAtLeast(0)
    val isScreenLoading = uiState.isLoading || (goalEntry == null && uiState.goals.isEmpty()) || isTasksLoading

    LaunchedEffect(goalEntry?.first) {
        if (goalEntry == null) {
            if (!uiState.isLoading) {
                tasks = emptyList()
                isTasksLoading = false
            }
            return@LaunchedEffect
        }
        isTasksLoading = true
        tasks = SubGoalUtils.getSubGoalsForParent(goalId).getOrDefault(emptyList())
        isTasksLoading = false
    }

    LaunchedEffect(uiState.celebrationEvent) {
        val event = uiState.celebrationEvent ?: return@LaunchedEffect
        NotificationUtils.showGoalAheadOfScheduleNotification(
            context = context,
            goalId = event.goalId,
            goalTitle = event.goalTitle,
            bonusXp = event.bonusXp
        )
        viewModel.consumeCelebrationEvent()
    }

    Scaffold(
        containerColor = TaskBg,
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
                }
            )
        }
    ) { innerPadding ->
        if (isScreenLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
        } else {
            val sortedTasks = tasks.sortedWith(
                compareByDescending<Pair<String, SubGoal>> { it.second.completed }.thenBy { it.second.title.lowercase() }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    HeaderRow(
                        title = goalName,
                        onBack = onNavigateBack
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    ProgressCard(
                        progress = progress,
                        completedTasks = completedTasks,
                        totalTasks = totalTasks
                    )
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }

                item {
                    MantraCard()
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sub-tasks",
                            color = appTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = Mint,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Remaining: $remainingTasks",
                                color = themedColor(Color(0xFF2D8D7B), Color(0xFF99F6E4)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (sortedTasks.isEmpty()) {
                    item {
                        Surface(
                            color = LightCard,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No sub-tasks yet.",
                                color = TaskMutedText,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    items(sortedTasks, key = { it.first }) { (taskId, task) ->
                        TaskItem(
                            task = task,
                            onToggle = {
                                scope.launch {
                                    val nextValue = !task.completed
                                    val result = SubGoalUtils.updateSubGoalStatus(taskId, nextValue)
                                    if (result.isSuccess) {
                                        val updated = tasks.map { entry ->
                                            if (entry.first == taskId) {
                                                entry.copy(second = entry.second.copy(completed = nextValue))
                                            } else {
                                                entry
                                            }
                                        }
                                        tasks = updated
                                        syncGoalProgress(
                                            goalId = goalId,
                                            viewModel = viewModel,
                                            subGoals = updated.map { it.second }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        onClick = { showAddTaskDialog = true },
                        color = PrimaryOrange.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, PrimaryOrange.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = null,
                                tint = PrimaryOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add New Sub-task",
                                color = PrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddSubTaskBottomSheet(
            taskTitle = newTaskTitle,
            onTaskTitleChange = { newTaskTitle = it },
            onDismiss = {
                showAddTaskDialog = false
                newTaskTitle = ""
            },
            onQuickAddSelected = { suggestion -> newTaskTitle = suggestion },
            canSave = newTaskTitle.isNotBlank() && goalEntry != null,
            onSave = {
                scope.launch {
                    val taskId = UUID.randomUUID().toString()
                    val newTask = SubGoal(
                        id = taskId,
                        title = newTaskTitle.trim(),
                        parentGoalId = goalId
                    )
                    val result = SubGoalUtils.addSubGoal(newTask)
                    if (result.isSuccess) {
                        val updated = tasks + (taskId to newTask)
                        tasks = updated
                        syncGoalProgress(
                            goalId = goalId,
                            viewModel = viewModel,
                            subGoals = updated.map { it.second }
                        )
                        showAddTaskDialog = false
                        newTaskTitle = ""
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSubTaskBottomSheet(
    taskTitle: String,
    onTaskTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onQuickAddSelected: (String) -> Unit,
    canSave: Boolean,
    onSave: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = LightCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 14.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(themedColor(Color(0xFFDCE2EA), Color(0xFF475569)))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add New Sub-Task",
                    color = appTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = onDismiss,
                    color = TaskInputBg,
                    shape = CircleShape,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TaskMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SUB-TASK TITLE",
                color = TaskMutedText,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = taskTitle,
                onValueChange = onTaskTitleChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Do 10 sun salutations",
                        color = themedColor(Color(0xFF94A3B8), Color(0xFF7C8EA3)),
                        fontSize = 16.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canSave) onSave() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TaskInputBg,
                    unfocusedContainerColor = TaskInputBg,
                    disabledContainerColor = TaskInputBg,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedTextColor = appTextPrimary,
                    unfocusedTextColor = appTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddChip(
                    text = "Quick add: Drink water",
                    textColor = PrimaryOrange,
                    containerColor = Color(0xFFFFF2EA),
                    borderColor = Color(0xFFFFDCC8),
                    onClick = { onQuickAddSelected("Drink water") }
                )
                QuickAddChip(
                    text = "Quick add: Stretch",
                    textColor = Color(0xFF3B82F6),
                    containerColor = Color(0xFFEEF4FF),
                    borderColor = Color(0xFFD8E6FF),
                    onClick = { onQuickAddSelected("Stretch") }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onSave,
                enabled = canSave,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF68B5C),
                    disabledContainerColor = Color(0xFFFCC3A8),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Sub-task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun QuickAddChip(
    text: String,
    textColor: Color,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun HeaderRow(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = appTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            modifier = Modifier.padding(horizontal = 56.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                color = LightCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TaskSoftBorder)
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = PrimaryOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Peach),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SelfImprovement,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressCard(
    progress: Int,
    completedTasks: Int,
    totalTasks: Int
) {
    Surface(
        color = LightCard,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 2.dp,
        shadowElevation = 7.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    color = PrimaryOrange,
                    trackColor = Color(0xFFFFEBDC),
                    strokeWidth = 7.dp,
                    modifier = Modifier.size(76.dp)
                )
                Text(
                    text = "$progress%",
                    color = appTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Goal",
                    color = appTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = "You've completed $completedTasks out of $totalTasks tasks today. Keep going!",
                    color = appTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun MantraCard() {
    Surface(
        color = Lavender,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DAILY MANTRA",
                    color = themedColor(Color(0xFF9333EA), Color(0xFFD8B4FE)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "\"The success of your day is defined by the peace you find in your morning.\"",
                color = themedColor(Color(0xFF6B21A8), Color(0xFFE9D5FF)),
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TaskItem(
    task: SubGoal,
    onToggle: () -> Unit
) {
    Surface(
        color = LightCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (task.completed) themedColor(Color(0xFFF1F4F8), Color(0xFF2A3548))
            else themedColor(Color(0xFFFFD6BF), Color(0xFF4A352A))
        ),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!task.completed) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PrimaryOrange.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (task.completed) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                    contentDescription = "Toggle",
                    tint = if (task.completed) PrimaryOrange else Color(0xFFD1A98A)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title.ifBlank { "Untitled Sub-task" },
                    color = if (task.completed) TaskMutedText else appTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = TaskMutedText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = estimateDuration(task.title),
                        color = TaskMutedText,
                        fontSize = 12.sp
                    )
                }
            }

            if (!task.completed) {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = null,
                    tint = themedColor(Color(0xFFD0D7E2), Color(0xFF64748B))
                )
            }
        }
    }
}

private fun estimateDuration(title: String): String {
    val words = title.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    val minutes = when {
        words >= 4 -> 15
        words >= 2 -> 10
        else -> 5
    }
    return "$minutes mins"
}

private fun syncGoalProgress(
    goalId: String,
    viewModel: GoalsViewModel,
    subGoals: List<SubGoal>
) {
    val nextProgress = if (subGoals.isEmpty()) 0 else SubGoalUtils.calculateProgressFromSubGoals(subGoals)
    viewModel.updateProgress(goalId, nextProgress)
}
