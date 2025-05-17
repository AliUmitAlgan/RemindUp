package com.aliumitalgan.remindup.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.components.*
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.ui.theme.AppDimensions
import com.aliumitalgan.remindup.ui.theme.SuccessGreen
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.SubGoal
import com.aliumitalgan.remindup.utils.SubGoalUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeGoals by remember { mutableStateOf<List<Pair<String, Goal>>>(emptyList()) }
    var completedGoals by remember { mutableStateOf<List<Pair<String, Goal>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Pair<String, Goal>?>(null) }
    var editingGoal by remember { mutableStateOf<Pair<String, Goal>?>(null) }

    // Alt hedefleri saklayacak Map (GoalId -> List<SubGoal>)
    var subGoalsMap by remember { mutableStateOf<Map<String, List<Pair<String, SubGoal>>>>(emptyMap()) }

    // Animasyon durumları
    var headerVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var fabVisible by remember { mutableStateOf(false) }

    // Bottom Navigation Items
    val bottomNavItems = listOf(
        BottomNavItem("Ana Sayfa", Icons.Filled.Home, Icons.Filled.Home, "home"),
        BottomNavItem("Hedefler", Icons.Filled.CheckCircle, Icons.Filled.CheckCircle, "goals"),
        BottomNavItem("Hatırlatıcılar", Icons.Filled.Notifications, Icons.Filled.Notifications, "reminders"),
        BottomNavItem("İlerleme", Icons.Filled.ShowChart, Icons.Filled.ShowChart, "progress"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Filled.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[1].route) }

    // Animasyonları başlat
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(300)
        contentVisible = true
        delay(600)
        fabVisible = true
    }

    // Tüm hedefleri ve alt hedefleri yükle
    LaunchedEffect(key1 = true) {
        try {
            // Hedefleri yükle
            val goalsResult = ProgressUtils.getUserGoals()
            if (goalsResult.isSuccess) {
                val goalsList = goalsResult.getOrDefault(emptyList())
                activeGoals = goalsList.filter { it.second.progress < 100 }
                completedGoals = goalsList.filter { it.second.progress >= 100 }

                // Alt hedefleri yükle
                val subGoalsMapTemp = mutableMapOf<String, List<Pair<String, SubGoal>>>()
                goalsList.forEach { (goalId, _) ->
                    try {
                        val subGoalsResult = SubGoalUtils.getSubGoalsForParent(goalId)
                        if (subGoalsResult.isSuccess) {
                            val subGoals = subGoalsResult.getOrDefault(emptyList())
                            if (subGoals.isNotEmpty()) {
                                subGoalsMapTemp[goalId] = subGoals
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GoalsScreen", "Error fetching sub-goals for goal: $goalId", e)
                    }
                }
                subGoalsMap = subGoalsMapTemp
            }
        } catch (e: Exception) {
            Log.e("GoalsScreen", "Unexpected error loading goals", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_goals)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                currentRoute = selectedNavItem,
                onItemSelected = { route ->
                    selectedNavItem = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> {} // Zaten hedefler ekranındayız
                        "reminders" -> onNavigateToReminders()
                        "progress" -> onNavigateToProgress()
                        "profile" -> onNavigateToSettings()
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                AnimatedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = Icons.Default.Add,
                    backgroundColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = fadeIn() + expandVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        ) {
                            HeaderCard(
                                title = stringResource(R.string.my_goals),
                                subtitle = if (activeGoals.isEmpty() && completedGoals.isEmpty())
                                    stringResource(R.string.start_by_adding_goal)
                                else
                                    "Hedeflerinizi takip edin ve başarıya ulaşın!",
                                icon = Icons.Default.Flag,
                                primaryColor = MaterialTheme.colorScheme.primary,
                                secondaryColor = MaterialTheme.colorScheme.secondary
                            ) {
                                // İçerik kısmı - İstatistikler
                                if (activeGoals.isNotEmpty() || completedGoals.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Aktif hedefler
                                        StatBadge(
                                            value = activeGoals.size.toString(),
                                            label = "Aktif",
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        // Tamamlanan hedefler
                                        StatBadge(
                                            value = completedGoals.size.toString(),
                                            label = "Tamamlanan",
                                            color = SuccessGreen
                                        )

                                        // Toplam hedefler
                                        StatBadge(
                                            value = (activeGoals.size + completedGoals.size).toString(),
                                            label = "Toplam",
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Boş durum
                    if (activeGoals.isEmpty() && completedGoals.isEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = contentVisible,
                                enter = fadeIn() + expandVertically()
                            ) {
                                EmptyGoalsView(onAddClick = { showAddDialog = true })
                            }
                        }
                    } else {
                        // Aktif Hedefler Başlığı
                        if (activeGoals.isNotEmpty()) {
                            item {
                                AnimatedVisibility(
                                    visible = contentVisible,
                                    enter = fadeIn() + slideInVertically { it / 2 }
                                ) {
                                    SectionHeader(
                                        title = stringResource(R.string.active_goals),
                                        subtitle = "Devam eden hedefleriniz"
                                    )
                                }
                            }
                        }

                        // Aktif Hedefler
                        items(activeGoals) { (id, goal) ->
                            key(id) {
                                AnimatedVisibility(
                                    visible = contentVisible,
                                    enter = fadeIn() + expandVertically(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                ) {
                                    GoalItem(
                                        goalId = id,
                                        goal = goal,
                                        subGoals = subGoalsMap[id]?.map { it.second } ?: emptyList(),
                                        onUpdateProgress = { newProgress ->
                                            coroutineScope.launch {
                                                updateGoalProgress(
                                                    goalId = id,
                                                    newProgress = newProgress,
                                                    onSuccess = {
                                                        // Hedefi aktif veya tamamlanmış listesine taşı
                                                        val updatedGoal = goal.copy(progress = newProgress)
                                                        if (newProgress >= 100) {
                                                            activeGoals = activeGoals.filterNot { it.first == id }
                                                            completedGoals = completedGoals + (id to updatedGoal)
                                                            showToast(context, StringResourcesProvider.getString(context, R.string.goal_completed))
                                                        } else {
                                                            activeGoals = activeGoals.map {
                                                                if (it.first == id) id to updatedGoal
                                                                else it
                                                            }
                                                            showToast(context, "İlerleme güncellendi: $newProgress%")
                                                        }
                                                    },
                                                    onError = { error ->
                                                        showToast(context, "Hata: $error")
                                                    }
                                                )
                                            }
                                        },
                                        onAddSubGoal = { subGoalTitle ->
                                            coroutineScope.launch {
                                                val currentUser = FirebaseAuth.getInstance().currentUser
                                                if (currentUser != null) {
                                                    val newSubGoalId = UUID.randomUUID().toString()
                                                    val newSubGoal = SubGoal(
                                                        id = newSubGoalId,
                                                        title = subGoalTitle,
                                                        parentGoalId = id,
                                                        userId = currentUser.uid,
                                                        completed = false
                                                    )

                                                    val result = SubGoalUtils.addSubGoal(newSubGoal)
                                                    if (result.isSuccess) {
                                                        showToast(context, "Alt hedef eklendi")

                                                        // UI'yi güncelle
                                                        val currentSubGoals = subGoalsMap[id]?.toMutableList() ?: mutableListOf()
                                                        currentSubGoals.add(Pair(newSubGoalId, newSubGoal))

                                                        subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                            put(id, currentSubGoals)
                                                        }

                                                        // İlerleme yüzdesini yeniden hesapla
                                                        val updatedSubGoals = currentSubGoals.map { it.second }
                                                        val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals)

                                                        updateGoalProgress(
                                                            goalId = id,
                                                            newProgress = newProgress,
                                                            onSuccess = {
                                                                // Hedefi aktif veya tamamlanmış listesine taşı
                                                                val updatedGoal = goal.copy(progress = newProgress)
                                                                if (newProgress >= 100) {
                                                                    activeGoals = activeGoals.filterNot { it.first == id }
                                                                    completedGoals = completedGoals + (id to updatedGoal)
                                                                } else {
                                                                    activeGoals = activeGoals.map {
                                                                        if (it.first == id) id to updatedGoal
                                                                        else it
                                                                    }
                                                                }
                                                            },
                                                            onError = { error ->
                                                                Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                            }
                                                        )
                                                    } else {
                                                        showToast(context, "Alt hedef eklenemedi: ${result.exceptionOrNull()?.message}")
                                                    }
                                                }
                                            }
                                        },
                                        onToggleSubGoal = { subGoal, isCompleted ->
                                            coroutineScope.launch {
                                                val result = SubGoalUtils.updateSubGoalStatus(subGoal.id, isCompleted)
                                                if (result.isSuccess) {
                                                    // UI'yi güncelle
                                                    val currentSubGoals = subGoalsMap[id]?.toMutableList() ?: mutableListOf()
                                                    val updatedSubGoals = currentSubGoals.map {
                                                        if (it.second.id == subGoal.id) {
                                                            Pair(it.first, it.second.copy(completed = isCompleted))
                                                        } else {
                                                            it
                                                        }
                                                    }

                                                    subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                        put(id, updatedSubGoals)
                                                    }

                                                    // İlerleme yüzdesini yeniden hesapla
                                                    val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals.map { it.second })

                                                    updateGoalProgress(
                                                        goalId = id,
                                                        newProgress = newProgress,
                                                        onSuccess = {
                                                            val updatedGoal = goal.copy(progress = newProgress)
                                                            if (newProgress >= 100) {
                                                                activeGoals = activeGoals.filterNot { it.first == id }
                                                                completedGoals = completedGoals + (id to updatedGoal)
                                                                showToast(context, StringResourcesProvider.getString(context, R.string.goal_completed))
                                                            } else {
                                                                activeGoals = activeGoals.map {
                                                                    if (it.first == id) id to updatedGoal
                                                                    else it
                                                                }
                                                            }
                                                        },
                                                        onError = { error ->
                                                            Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                        }
                                                    )
                                                } else {
                                                    showToast(context, "Alt hedef güncellenemedi: ${result.exceptionOrNull()?.message}")
                                                }
                                            }
                                        },
                                        onDeleteSubGoal = { subGoal ->
                                            coroutineScope.launch {
                                                val result = SubGoalUtils.deleteSubGoal(subGoal.id)
                                                if (result.isSuccess) {
                                                    showToast(context, "Alt hedef silindi")

                                                    // UI'yi güncelle
                                                    val updatedSubGoals = subGoalsMap[id]?.filter { it.second.id != subGoal.id } ?: emptyList()

                                                    // Map'i güncelle
                                                    subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                        put(id, updatedSubGoals)
                                                    }

                                                    // İlerleme yüzdesini yeniden hesapla
                                                    val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals.map { it.second })

                                                    updateGoalProgress(
                                                        goalId = id,
                                                        newProgress = newProgress,
                                                        onSuccess = {
                                                            val updatedGoal = goal.copy(progress = newProgress)
                                                            if (newProgress >= 100) {
                                                                activeGoals = activeGoals.filterNot { it.first == id }
                                                                completedGoals = completedGoals + (id to updatedGoal)
                                                            } else {
                                                                activeGoals = activeGoals.map {
                                                                    if (it.first == id) id to updatedGoal
                                                                    else it
                                                                }
                                                            }
                                                        },
                                                        onError = { error ->
                                                            Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                        }
                                                    )
                                                } else {
                                                    showToast(context, "Alt hedef silinemedi: ${result.exceptionOrNull()?.message}")
                                                }
                                            }
                                        },
                                        onDeleteGoal = {
                                            showDeleteConfirmDialog = id to goal
                                        }
                                    )
                                }
                            }
                        }

                        // Tamamlanan Hedefler Başlığı
                        if (completedGoals.isNotEmpty()) {
                            item {
                                AnimatedVisibility(
                                    visible = contentVisible,
                                    enter = fadeIn() + slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(durationMillis = 300, delayMillis = 150)
                                    )
                                ) {
                                    SectionHeader(
                                        title = stringResource(R.string.completed_goals),
                                        subtitle = "Tebrikler! Tamamladığınız hedefler"
                                    )
                                }
                            }
                        }

                        // Tamamlanan Hedefler
                        items(completedGoals) { (id, goal) ->
                            key(id) {
                                AnimatedVisibility(
                                    visible = contentVisible,
                                    enter = fadeIn() + expandVertically(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                ) {
                                    CompletedGoalItem(
                                        goal = goal,
                                        onDeleteGoal = {
                                            showDeleteConfirmDialog = id to goal
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Add extra space at bottom
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Hedef Ekleme Diyaloğu
            if (showAddDialog) {
                AddGoalBottomSheet(
                    onDismiss = { showAddDialog = false },
                    onSave = { newGoal ->
                        coroutineScope.launch {
                            addGoal(
                                goal = newGoal,
                                onSuccess = { id ->
                                    activeGoals = activeGoals + (id to newGoal)
                                    subGoalsMap = subGoalsMap.toMutableMap().apply {
                                        put(id, emptyList())
                                    }
                                    showToast(context, "Hedef eklendi")
                                },
                                onError = { error ->
                                    showToast(context, "Hedef eklenemedi: $error")
                                }
                            )
                        }
                    }
                )
            }

            // Hedef Silme Doğrulama Dialog'u
            showDeleteConfirmDialog?.let { (goalId, goal) ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text(stringResource(R.string.delete_goal)) },
                    text = {
                        Text(
                            "\"${goal.title}\" ${stringResource(R.string.delete_goal_confirmation)}"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        // Önce alt hedefleri sil
                                        val subGoalsResult = SubGoalUtils.deleteAllSubGoalsForParent(goalId)

                                        if (subGoalsResult.isSuccess) {
                                            // Şimdi hedefi sil
                                            deleteGoal(
                                                goalId = goalId,
                                                onSuccess = {
                                                    // Listelerden kaldır
                                                    activeGoals = activeGoals.filterNot { it.first == goalId }
                                                    completedGoals = completedGoals.filterNot { it.first == goalId }

                                                    // SubGoalsMap'ten kaldır
                                                    subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                        remove(goalId)
                                                    }

                                                    showToast(context, "Hedef silindi")
                                                },
                                                onError = { error ->
                                                    showToast(context, "Hedef silinemedi: $error")
                                                }
                                            )
                                        } else {
                                            showToast(context, "Alt hedefler silinemedi: ${subGoalsResult.exceptionOrNull()?.message}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("GoalsScreen", "Error deleting goal", e)
                                        showToast(context, "Hedef silinirken hata oluştu: ${e.message}")
                                    }
                                }
                                showDeleteConfirmDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.yes_delete))
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = null },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}

@Composable
fun StatBadge(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoalItem(
    goalId: String,
    goal: Goal,
    subGoals: List<SubGoal>,
    onUpdateProgress: (Int) -> Unit,
    onAddSubGoal: (String) -> Unit,
    onToggleSubGoal: (SubGoal, Boolean) -> Unit,
    onDeleteSubGoal: (SubGoal) -> Unit,
    onDeleteGoal: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showSubGoalDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }

    val progressColor = when {
        goal.progress >= 100 -> SuccessGreen
        goal.progress >= 75 -> MaterialTheme.colorScheme.secondary
        goal.progress >= 50 -> MaterialTheme.colorScheme.primary
        goal.progress >= 25 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    }

    EnhancedCard(
        primaryColor = progressColor,
        cornerRadius = 20.dp,
        elevation = if (expanded) 8.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            // Header with toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon in circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(progressColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and progress
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${goal.progress}% ${stringResource(R.string.in_progress)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expand arrow
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Daralt" else "Genişlet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            ProgressCard(
                title = "İlerleme",
                progress = goal.progress.toFloat(),
                maxProgress = 100f,
                primaryColor = progressColor,
                animationDuration = 800
            )

            // Expanded content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                EnhancedDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-goals section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.sub_goals)} (${subGoals.count { it.completed }}/${subGoals.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    EnhancedButton(
                        text = stringResource(R.string.add_sub_goal),
                        onClick = { showSubGoalDialog = true },
                        icon = Icons.Default.Add,
                        backgroundColor = progressColor,
                        cornerRadius = 8.dp,
                        modifier = Modifier.height(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-goals list
                if (subGoals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_sub_goals),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subGoals.forEach { subGoal ->
                            SubGoalItem(
                                subGoal = subGoal,
                                onToggle = { isCompleted -> onToggleSubGoal(subGoal, isCompleted) },
                                onDelete = { onDeleteSubGoal(subGoal) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                EnhancedDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Progress Update Button
                    EnhancedButton(
                        text = stringResource(R.string.update),
                        onClick = { showProgressDialog = true },
                        icon = Icons.Default.Update,
                        backgroundColor = progressColor,
                        modifier = Modifier.weight(1f)
                    )

                    // Delete Button
                    EnhancedButton(
                        text = stringResource(R.string.delete),
                        onClick = onDeleteGoal,
                        icon = Icons.Default.Delete,
                        backgroundColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Add Sub-Goal Dialog
    if (showSubGoalDialog) {
        var newSubGoalTitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSubGoalDialog = false },
            title = { Text(stringResource(R.string.add_sub_goal)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSubGoalTitle,
                        onValueChange = { newSubGoalTitle = it },
                        label = { Text("Alt Hedef Başlığı") },
                        placeholder = { Text(stringResource(R.string.sub_goal_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = progressColor,
                            focusedLabelColor = progressColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubGoalTitle.isNotBlank()) {
                            onAddSubGoal(newSubGoalTitle)
                            showSubGoalDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = progressColor
                    ),
                    enabled = newSubGoalTitle.isNotBlank()
                ) {
                    Text(stringResource(R.string.add_sub_goal))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSubGoalDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Progress Update Dialog
    if (showProgressDialog) {
        var newProgress by remember { mutableStateOf(goal.progress) }

        AlertDialog(
            onDismissRequest = { showProgressDialog = false },
            title = { Text(stringResource(R.string.edit_progress)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Current progress
                    Text(
                        text = stringResource(R.string.current_progress, goal.progress),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress value display
                    Text(
                        text = "$newProgress%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            newProgress >= 100 -> SuccessGreen
                            newProgress >= 75 -> MaterialTheme.colorScheme.secondary
                            newProgress >= 50 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Slider
                    Slider(
                        value = newProgress.toFloat(),
                        onValueChange = { newProgress = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 20,
                        colors = SliderDefaults.colors(
                            thumbColor = progressColor,
                            activeTrackColor = progressColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Completion notice
                    if (newProgress == 100) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = SuccessGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = stringResource(R.string.completing_goal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProgress(newProgress)
                        showProgressDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = progressColor
                    )
                ) {
                    Text(stringResource(R.string.update))
                }
            },
            dismissButton = {
                TextButton(onClick = { showProgressDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SubGoalItem(
    subGoal: SubGoal,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (subGoal.completed)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else
            MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = subGoal.completed,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )

            Text(
                text = subGoal.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (subGoal.completed)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (subGoal.completed)
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                else
                    null,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CompletedGoalItem(
    goal: Goal,
    onDeleteGoal: () -> Unit
) {
    EnhancedCard(
        primaryColor = SuccessGreen,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Success indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and status
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(R.string.completed),
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Delete button
            IconButton(onClick = onDeleteGoal) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmptyGoalsView(
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animation for the empty state icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.no_goals),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.start_by_adding_goal),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            EnhancedButton(
                text = stringResource(R.string.add_goal),
                onClick = onAddClick,
                icon = Icons.Default.Add,
                cornerRadius = 16.dp
            )
        }
    }
}



// Hedefleri yükle
private suspend fun loadGoals(
    onSuccess: (List<Pair<String, Goal>>) -> Unit,
    onError: (String) -> Unit
) {
    val result = ProgressUtils.getUserGoals()
    if (result.isSuccess) {
        onSuccess(result.getOrDefault(emptyList()))
    } else {
        onError(result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
    }
}

// Hedef ekle
private suspend fun addGoal(
    goal: Goal,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Kullanıcı ID'sini ekleyerek hedefi oluştur
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val goalWithUser = goal.copy(userId = userId ?: "")

        val docRef = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("goals")
            .add(goalWithUser)
            .await()

        onSuccess(docRef.id)
    } catch (e: Exception) {
        onError(e.message ?: "Bilinmeyen hata")
    }
}

// Hedef güncelle
private suspend fun updateGoal(
    goalId: String,
    goal: Goal,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("goals")
            .document(goalId)
            .set(goal)
            .await()

        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "Bilinmeyen hata")
    }
}

// Hedef sil
private suspend fun deleteGoal(
    goalId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("goals")
            .document(goalId)
            .delete()
            .await()

        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "Bilinmeyen hata")
    }
}

// İlerleme güncelle
private suspend fun updateGoalProgress(
    goalId: String,
    newProgress: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val result = ProgressUtils.updateGoalProgress(goalId, newProgress)
    if (result.isSuccess) {
        onSuccess()
    } else {
        onError(result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
    }
}



// StringResourcesProvider nesnesi, R.string.* değerlerini context ile kullanmak için
object StringResourcesProvider {
    fun getString(context: Context, resId: Int): String {
        return context.getString(resId)
    }

    fun getString(context: Context, resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}