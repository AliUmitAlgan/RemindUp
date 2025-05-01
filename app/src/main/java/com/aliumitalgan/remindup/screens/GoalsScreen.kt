package com.aliumitalgan.remindup.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.aliumitalgan.remindup.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.NestedGoalCard
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.StringResourcesProvider
import com.aliumitalgan.remindup.utils.SubGoal
import com.aliumitalgan.remindup.utils.SubGoalUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
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
    var editingGoal by remember { mutableStateOf<Pair<String, Goal>?>(null) }

    // Alt hedefleri saklayacak Map (GoalId -> List<SubGoal>)
    var subGoalsMap by remember { mutableStateOf<Map<String, List<Pair<String, SubGoal>>>>(emptyMap()) }

    // Bottom Navigation Items
    val bottomNavItems = listOf(
        BottomNavItem("Ana Sayfa", Icons.Filled.Home, Icons.Filled.Home, "home"),
        BottomNavItem("Hedefler", Icons.Filled.CheckCircle, Icons.Filled.CheckCircle, "goals"),
        BottomNavItem("Hatırlatıcılar", Icons.Filled.Notifications, Icons.Filled.Notifications, "reminders"),
        BottomNavItem("İlerleme", Icons.Filled.ShowChart, Icons.Filled.ShowChart, "progress"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Filled.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[1].route) }

    // Tüm hedefleri ve alt hedefleri yükle
    LaunchedEffect(key1 = true) {
        loadGoals(
            onSuccess = { goalsList ->
                // Hedefleri aktif ve tamamlanmış olarak ayır
                activeGoals = goalsList.filter { it.second.progress < 100 }
                completedGoals = goalsList.filter { it.second.progress >= 100 }

                // Her hedef için alt hedefleri yükle
                val subGoalsMapTemp = mutableMapOf<String, List<Pair<String, SubGoal>>>()
                coroutineScope.launch {
                    goalsList.forEach { (goalId, _) ->
                        try {
                            val subGoalsResult = SubGoalUtils.getSubGoalsForParent(goalId)
                            if (subGoalsResult.isSuccess) {
                                subGoalsMapTemp[goalId] = subGoalsResult.getOrDefault(emptyList())
                            }
                        } catch (e: Exception) {
                            // Alt hedefler yüklenemedi, boş liste ile devam et
                        }
                    }
                    subGoalsMap = subGoalsMapTemp
                    isLoading = false
                }
            },
            onError = { error ->
                showToast(context, "Hedefler yüklenemedi: $error")
                isLoading = false
            }
        )
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
                actions = {
                    // Ekleme butonu üst kısımda
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Hedef Ekle")
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // İçerik
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (activeGoals.isEmpty() && completedGoals.isEmpty()) {
                EmptyGoalsView(
                    onAddClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Aktif Hedefler Başlığı
                    if (activeGoals.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.in_progress),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    // Aktif Hedefler
                    items(activeGoals.size) { index ->
                        val (id, goal) = activeGoals[index]
                        val subGoals = subGoalsMap[id]?.map { it.second } ?: emptyList()

                        NestedGoalCard(
                            goalTitle = goal.title,
                            goalProgress = goal.progress,
                            subGoals = subGoals,
                            onProgressUpdate = { newProgress ->
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
                                                showToast(context, StringResourcesProvider.getString(context, R.string.progress_updated, newProgress))
                                            }
                                        },
                                        onError = { error ->
                                            showToast(context, StringResourcesProvider.getString(context, R.string.edit_progress, error))
                                        }
                                    )
                                }
                            },
                            // Diğer fonksiyonlar aynı kalacak
                            onAddSubGoal = { subGoalTitle ->
                                // Önceki kod aynı kalacak
                            },
                            onToggleSubGoal = { subGoal, isCompleted ->
                                // Önceki kod aynı kalacak
                            }
                        )
                    }

                    // Tamamlanmış Hedefler Başlığı
                    if (completedGoals.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.completed_goals),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Tamamlanmış Hedefler
                    items(completedGoals.size) { index ->
                        val (id, goal) = completedGoals[index]
                        val subGoals = subGoalsMap[id]?.map { it.second } ?: emptyList()

                        NestedGoalCard(
                            goalTitle = goal.title,
                            goalProgress = goal.progress,
                            subGoals = subGoals,
                            onProgressUpdate = { newProgress ->
                                // Gerekirse düzenleme yapılabilir
                            }
                        )
                    }
                }
            }

            // Hedef Ekleme Diyaloğu
            if (showAddDialog) {
                ModernGoalDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { title, progress ->
                        coroutineScope.launch {
                            val newGoal = Goal(title = title, progress = progress)
                            addGoal(
                                goal = newGoal,
                                onSuccess = { id ->
                                    activeGoals = activeGoals + (id to newGoal)
                                    // Yeni hedef için boş alt hedef listesi oluştur
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
                        showAddDialog = false
                    }
                )
            }

            // Hedef Düzenleme Diyaloğu
            editingGoal?.let { (id, goal) ->
                ModernGoalDialog(
                    title = "Hedefi Düzenle",
                    initialTitle = goal.title,
                    initialProgress = goal.progress,
                    onDismiss = { editingGoal = null },
                    onSave = { title, progress ->
                        coroutineScope.launch {
                            val updatedGoal = Goal(title = title, progress = progress, userId = goal.userId)
                            updateGoal(
                                goalId = id,
                                goal = updatedGoal,
                                onSuccess = {
                                    // Hedefi aktif veya tamamlanmış listesine taşı
                                    if (progress >= 100) {
                                        activeGoals = activeGoals.filterNot { it.first == id }
                                        completedGoals = completedGoals + (id to updatedGoal)
                                    } else {
                                        activeGoals = activeGoals.map {
                                            if (it.first == id) id to updatedGoal
                                            else it
                                        }
                                        completedGoals = completedGoals.filterNot { it.first == id }
                                    }
                                    showToast(context, "Hedef güncellendi")
                                },
                                onError = { error ->
                                    showToast(context, "Hedef güncellenemedi: $error")
                                }
                            )
                        }
                        editingGoal = null
                    }
                )
            }
        }
    }
}
@Composable
fun ModernGoalDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    title: String = "Hedef Ekle",
    initialTitle: String = "",
    initialProgress: Int = 0
) {
    var goalTitle by remember { mutableStateOf(initialTitle) }
    var goalProgress by remember { mutableStateOf(initialProgress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AddTask,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hedef Başlığı
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { stringResource(R.string.goal_title) },
                    placeholder = { stringResource(R.string.sub_goal_hint) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // İlerleme Slider
                Text(
                    text = stringResource(R.string.progress, goalProgress),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = goalProgress.toFloat(),
                    onValueChange = { goalProgress = it.toInt() },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (goalTitle.isNotBlank()) {
                        onSave(goalTitle, goalProgress)
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
@Composable
fun EmptyGoalsView(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
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
                imageVector = Icons.Default.Flag,
                contentDescription = "Hedefler",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Henüz hedef bulunmuyor",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Yeni bir hedef ekleyerek başlayın",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Ekle"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_goal))
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

// Toast mesajı göster
// Toast mesajı göster
