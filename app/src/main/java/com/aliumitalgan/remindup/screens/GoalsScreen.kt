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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

private val GoalsBackground = Color(0xFFF8F3EF)
private val SweetOrange = Color(0xFFF26522)
private val MutedBlue = Color(0xFF5B8DE8)
private val MutedPurple = Color(0xFFA77CF4)
private val MutedMint = Color(0xFF7FD7B3)

@Composable
fun GoalsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {}
) {
    var goals by remember { mutableStateOf<List<Pair<String, Goal>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All Goals") }
    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Health") }
    var isSaving by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("goals") }
    val coroutineScope = rememberCoroutineScope()
    val navItems = mainBottomNavItems()

    suspend fun refreshGoals() {
        isLoading = true
        val result = ProgressUtils.getUserGoals()
        goals = result.getOrDefault(emptyList())
        isLoading = false
    }

    LaunchedEffect(Unit) {
        refreshGoals()
    }

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
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> Unit
                        "progress" -> onNavigateToProgress()
                        "settings" -> onNavigateToSettings()
                    }
                },
                onCenterActionClick = { showAddDialog = true }
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
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD8C2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SweetOrange, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RemindUp Sweet Goals",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B2240),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = SweetOrange)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All Goals", "Health", "Learning", "Work").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (filter) {
                                        "Health" -> Color(0xFFDCEAFD)
                                        "Learning" -> Color(0xFFEADFFC)
                                        "Work" -> Color(0xFFD8F2E6)
                                        else -> SweetOrange
                                    },
                                    selectedLabelColor = if (filter == "All Goals") Color.White else Color(0xFF223053)
                                )
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "My Sweet Progress",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Color(0xFF1A2140)
                    )
                    Text(
                        text = "You have ${filteredGoals.size} active goals today!",
                        color = Color(0xFF7D86A0),
                        fontSize = 13.sp
                    )
                }

                items(filteredGoals) { (goalId, goal) ->
                    SweetGoalCard(
                        goal = goal,
                        onAddLog = {
                            val nextProgress = (goal.progress + 10).coerceAtMost(100)
                            coroutineScope.launch {
                                ProgressUtils.updateGoalProgress(goalId, nextProgress)
                                refreshGoals()
                            }
                        }
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF7E5DB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unlock Premium Gems", color = SweetOrange, fontWeight = FontWeight.ExtraBold)
                                Text(
                                    "Get 3D animated icons and advanced analytics for your goals.",
                                    color = Color(0xFF8B6F63),
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("PRO", color = SweetOrange, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                isSaving = false
            },
            title = { Text("Add New Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Goal title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Description") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Category (Health/Learning/Work)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSaving && titleInput.isNotBlank(),
                    onClick = {
                        isSaving = true
                        coroutineScope.launch {
                            addGoal(
                                title = titleInput.trim(),
                                description = descInput.trim(),
                                categoryName = categoryInput.trim().ifBlank { "Health" }
                            )
                            titleInput = ""
                            descInput = ""
                            categoryInput = "Health"
                            showAddDialog = false
                            isSaving = false
                            refreshGoals()
                        }
                    }
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SweetGoalCard(
    goal: Goal,
    onAddLog: () -> Unit
) {
    val accent = when (mapCategory(goal.category)) {
        "Health" -> MutedBlue
        "Learning" -> MutedPurple
        "Work" -> MutedMint
        else -> Color(0xFFE6ECF5)
    }
    val icon = when (mapCategory(goal.category)) {
        "Health" -> Icons.Filled.LocalFlorist
        "Learning" -> Icons.Filled.School
        "Work" -> Icons.Filled.Work
        else -> Icons.Filled.Language
    }

    Surface(
        color = accent.copy(alpha = 0.18f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    mapCategory(goal.category).uppercase(Locale.getDefault()),
                    fontSize = 10.sp,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text("${goal.progress}%", color = accent, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = goal.title.ifBlank { "Untitled Goal" },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 23.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1D2748)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((goal.progress.coerceIn(0, 100)) / 100f)
                        .height(8.dp)
                        .background(accent)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (goal.description.isBlank()) "Keep going!" else goal.description,
                    color = Color(0xFF8089A3),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAddLog) {
                    Text("Add Log", color = accent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun mapCategory(category: Int): String {
    return when (category) {
        1 -> "Health"
        2 -> "Learning"
        3 -> "Work"
        else -> "Health"
    }
}

private suspend fun addGoal(
    title: String,
    description: String,
    categoryName: String
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val category = when (categoryName.lowercase(Locale.getDefault())) {
        "learning" -> 2
        "work" -> 3
        else -> 1
    }

    val goal = Goal(
        title = title,
        description = description,
        progress = 0,
        category = category,
        userId = uid
    )

    FirebaseFirestore.getInstance()
        .collection("goals")
        .add(goal)
        .await()
}
