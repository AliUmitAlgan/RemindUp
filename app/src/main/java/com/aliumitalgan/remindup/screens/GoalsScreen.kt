package com.aliumitalgan.remindup.screens

import android.util.Log
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
import java.util.UUID


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
    var showDeleteConfirmDialog by remember { mutableStateOf<Pair<String, Goal>?>(null) }

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
    // When loading sub-goals
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            try {
                // Load goals
                val goalsResult = ProgressUtils.getUserGoals()
                if (goalsResult.isSuccess) {
                    val goalsList = goalsResult.getOrDefault(emptyList())
                    activeGoals = goalsList.filter { it.second.progress < 100 }
                    completedGoals = goalsList.filter { it.second.progress >= 100 }

                    // Load sub-goals for each goal
                    val subGoalsMapTemp = mutableMapOf<String, List<Pair<String, SubGoal>>>()
                    goalsList.forEach { (goalId, _) ->
                        try {
                            Log.d("GoalsScreen", "Fetching sub-goals for goal: $goalId")
                            val subGoalsResult = SubGoalUtils.getSubGoalsForParent(goalId)

                            Log.d("GoalsScreen", "Sub-goals fetch result for $goalId: ${subGoalsResult.isSuccess}")

                            if (subGoalsResult.isSuccess) {
                                val subGoals = subGoalsResult.getOrDefault(emptyList())
                                Log.d("GoalsScreen", "Sub-goals for $goalId: $subGoals")

                                if (subGoals.isNotEmpty()) {
                                    subGoalsMapTemp[goalId] = subGoals
                                }
                            } else {
                                Log.e("GoalsScreen", "Failed to fetch sub-goals for goal: $goalId")
                            }
                        } catch (e: Exception) {
                            Log.e("GoalsScreen", "Error fetching sub-goals for goal: $goalId", e)
                        }
                    }

                    subGoalsMap = subGoalsMapTemp
                    Log.d("GoalsScreen", "Final sub-goals map: $subGoalsMap")
                } else {
                    Log.e("GoalsScreen", "Failed to load goals")
                }
            } catch (e: Exception) {
                Log.e("GoalsScreen", "Unexpected error loading goals", e)
            } finally {
                isLoading = false
            }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)  // Elemanlar arasında eşit boşluk
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
                            // Add Sub Goal
                            onAddSubGoal = { subGoalTitle ->
                                coroutineScope.launch {
                                    try {
                                        // Kullanıcı kontrolü
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        if (currentUser == null) {
                                            Log.e("GoalsScreen", "User not logged in")
                                            Toast.makeText(context, "Kullanıcı girişi yapılmamış", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }

                                        // Subgoal oluştur
                                        val newSubGoalId = UUID.randomUUID().toString()
                                        val newSubGoal = SubGoal(
                                            id = newSubGoalId,
                                            title = subGoalTitle,
                                            parentGoalId = id, // id, for döngüsünden gelen goal id'si
                                            userId = currentUser.uid,
                                            completed = false
                                        )

                                        Log.d("GoalsScreen", "Adding new subgoal: $newSubGoal")

                                        // SubGoal'ı Firestore'a ekle
                                        val result = SubGoalUtils.addSubGoal(newSubGoal)

                                        if (result.isSuccess) {
                                            Log.d("GoalsScreen", "Subgoal successfully added with ID: ${result.getOrNull()}")
                                            Toast.makeText(context, "Alt hedef eklendi", Toast.LENGTH_SHORT).show()

                                            // Eklenen alt hedefi mevcut listeye ekle (UI'yi hemen güncellemek için)
                                            val currentSubGoals = subGoalsMap[id]?.toMutableList() ?: mutableListOf()
                                            currentSubGoals.add(Pair(newSubGoalId, newSubGoal))

                                            // Map'i güncelleyerek state'i değiştir
                                            subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                put(id, currentSubGoals)
                                            }

                                            // Yeni ilerleme durumunu hesapla
                                            val updatedSubGoals = currentSubGoals.map { it.second }
                                            val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals)

                                            // Hedefin ilerleme durumunu güncelle
                                            updateGoalProgress(
                                                goalId = id,
                                                newProgress = newProgress,
                                                onSuccess = {
                                                    Log.d("GoalsScreen", "Goal progress updated to $newProgress%")

                                                    // UI'yi güncelle - active ve completed listelerini güncelle
                                                    if (newProgress >= 100) {
                                                        // Hedef tamamlandı, active'den remove et, completed'e ekle
                                                        val goalToUpdate = activeGoals.find { it.first == id }?.second
                                                        if (goalToUpdate != null) {
                                                            val updatedGoal = goalToUpdate.copy(progress = newProgress)
                                                            activeGoals = activeGoals.filter { it.first != id }
                                                            completedGoals = completedGoals + (id to updatedGoal)
                                                        }
                                                    } else {
                                                        // Hedef hala aktif, ilerlemesini güncelle
                                                        activeGoals = activeGoals.map {
                                                            if (it.first == id) id to it.second.copy(progress = newProgress)
                                                            else it
                                                        }
                                                    }
                                                },
                                                onError = { error ->
                                                    Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                    Toast.makeText(context, "Hedef ilerlemesi güncellenirken hata oluştu", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            // Alt hedef ekleme hatası
                                            Log.e("GoalsScreen", "Error adding subgoal: ${result.exceptionOrNull()?.message}")
                                            Toast.makeText(context, "Alt hedef eklenemedi: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        // Genel hata
                                        Log.e("GoalsScreen", "Unexpected error adding subgoal", e)
                                        Toast.makeText(context, "Beklenmeyen hata: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            // Alt hedef silme işlemi
                            onDeleteSubGoal = { subGoal ->
                                coroutineScope.launch {
                                    try {
                                        // Alt hedefi sil
                                        val result = SubGoalUtils.deleteSubGoal(subGoal.id)

                                        if (result.isSuccess) {
                                            Toast.makeText(context, context.getString(R.string.sub_goal_deleted), Toast.LENGTH_SHORT).show()

                                            // UI'yi güncelle
                                            val updatedSubGoals = subGoalsMap[id]?.filter { it.second.id != subGoal.id } ?: emptyList()

                                            // Map'i güncelle
                                            subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                put(id, updatedSubGoals)
                                            }

                                            // Hedefin ilerleme yüzdesini güncelle
                                            val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals.map { it.second })

                                            updateGoalProgress(
                                                goalId = id,
                                                newProgress = newProgress,
                                                onSuccess = {
                                                    // Hedefin ilerleme durumunu güncelle
                                                    if (newProgress >= 100) {
                                                        val goalToUpdate = activeGoals.find { it.first == id }?.second
                                                        if (goalToUpdate != null) {
                                                            val updatedGoal = goalToUpdate.copy(progress = newProgress)
                                                            activeGoals = activeGoals.filter { it.first != id }
                                                            completedGoals = completedGoals + (id to updatedGoal)
                                                        }
                                                    } else {
                                                        // Hedef hala aktif, ilerlemesini güncelle
                                                        activeGoals = activeGoals.map {
                                                            if (it.first == id) id to it.second.copy(progress = newProgress)
                                                            else it
                                                        }
                                                    }
                                                },
                                                onError = { error ->
                                                    Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                }
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Alt hedef silinemedi: ${result.exceptionOrNull()?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("GoalsScreen", "Error deleting subgoal", e)
                                        Toast.makeText(
                                            context,
                                            "Alt hedef silinirken hata oluştu: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            // Toggle sub goal
                            onToggleSubGoal = { subGoal, isCompleted ->
                                // Use a callback approach or move coroutine logic outside of composable
                                val toggleSubGoal: (SubGoal, Boolean) -> Unit = { goal, completed ->
                                    coroutineScope.launch {
                                        try {
                                            // Alt hedefin tamamlanma durumunu güncelle
                                            val result = SubGoalUtils.updateSubGoalStatus(goal.id, completed)

                                            if (result.isSuccess) {
                                                // Alt hedef durumu başarıyla güncellendi
                                                // Alt hedefleri yeniden yükle
                                                val updatedSubGoalsResult = SubGoalUtils.getSubGoalsForParent(id)

                                                if (updatedSubGoalsResult.isSuccess) {
                                                    // Alt hedef listesini güncelle
                                                    subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                        put(id, updatedSubGoalsResult.getOrDefault(emptyList()))
                                                    }

                                                    // Alt hedeflere göre hedefin ilerlemesini hesapla
                                                    val subGoals = updatedSubGoalsResult.getOrDefault(emptyList())
                                                    val newProgress = SubGoalUtils.calculateProgressFromSubGoals(subGoals.map { it.second })

                                                    // Hedef ilerlemesini güncelle
                                                    updateGoalProgress(
                                                        goalId = id,
                                                        newProgress = newProgress,
                                                        onSuccess = {
                                                            // Başarılı güncelleme
                                                            Toast.makeText(
                                                                context,
                                                                if (completed)
                                                                    context.getString(R.string.sub_goal_completed)
                                                                else
                                                                    "Alt hedef güncellendi",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            // Arayüzü güncelle - active ve completed listelerini güncelle
                                                            if (newProgress >= 100) {
                                                                // Hedef tamamlandı, active'den remove et, completed'e ekle
                                                                val goalToUpdate = activeGoals.find { it.first == id }?.second
                                                                if (goalToUpdate != null) {
                                                                    val updatedGoal = goalToUpdate.copy(progress = newProgress)
                                                                    activeGoals = activeGoals.filter { it.first != id }
                                                                    completedGoals = completedGoals + (id to updatedGoal)
                                                                }
                                                            } else {
                                                                // Hedef hala aktif, ilerlemesini güncelle
                                                                activeGoals = activeGoals.map {
                                                                    if (it.first == id) id to it.second.copy(progress = newProgress)
                                                                    else it
                                                                }
                                                            }
                                                        },
                                                        onError = { error ->
                                                            // Güncelleme hatası
                                                            Toast.makeText(
                                                                context,
                                                                "Hedef güncellenemedi: $error",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    )
                                                } else {
                                                    // Alt hedefleri yüklerken hata
                                                    Toast.makeText(
                                                        context,
                                                        "Alt hedefler yüklenemedi",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                // Alt hedef durumu güncellenirken hata
                                                Toast.makeText(
                                                    context,
                                                    "Alt hedef güncellenemedi: ${result.exceptionOrNull()?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            // Beklenmeyen hata
                                            Toast.makeText(
                                                context,
                                                "Beklenmeyen bir hata oluştu: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e("GoalsScreen", "Sub-goal toggle error", e)
                                        }
                                    }
                                }

                                // Call the function
                                toggleSubGoal(subGoal, isCompleted)
                            },
                            // Hedefi silme fonksiyonu
                            onDeleteGoal = {
                                // Hedefi silmek için doğrulama dialog'u göster
                                showDeleteConfirmDialog = id to goal
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
                                coroutineScope.launch {
                                    updateGoalProgress(
                                        goalId = id,
                                        newProgress = newProgress,
                                        onSuccess = {
                                            // Hedefi aktif veya tamamlanmış listesine taşı
                                            val updatedGoal = goal.copy(progress = newProgress)
                                            if (newProgress < 100) {
                                                completedGoals = completedGoals.filterNot { it.first == id }
                                                activeGoals = activeGoals + (id to updatedGoal)
                                                showToast(context, "Hedef aktif duruma getirildi")
                                            } else {
                                                completedGoals = completedGoals.map {
                                                    if (it.first == id) id to updatedGoal
                                                    else it
                                                }
                                                showToast(context, "Hedef güncellendi")
                                            }
                                        },
                                        onError = { error ->
                                            showToast(context, "Hedef güncellenemedi: $error")
                                        }
                                    )
                                }
                            },
                            // Alt hedef ekleme
                            onAddSubGoal = { subGoalTitle ->
                                coroutineScope.launch {
                                    try {
                                        // Kullanıcı kontrolü
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        if (currentUser == null) {
                                            Log.e("GoalsScreen", "User not logged in")
                                            Toast.makeText(context, "Kullanıcı girişi yapılmamış", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }

                                        // Subgoal oluştur
                                        val newSubGoalId = UUID.randomUUID().toString()
                                        val newSubGoal = SubGoal(
                                            id = newSubGoalId,
                                            title = subGoalTitle,
                                            parentGoalId = id,
                                            userId = currentUser.uid,
                                            completed = false
                                        )

                                        // SubGoal'ı Firestore'a ekle
                                        val result = SubGoalUtils.addSubGoal(newSubGoal)

                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Alt hedef eklendi", Toast.LENGTH_SHORT).show()

                                            // UI'yi güncelle
                                            val currentSubGoals = subGoalsMap[id]?.toMutableList() ?: mutableListOf()
                                            currentSubGoals.add(Pair(newSubGoalId, newSubGoal))

                                            subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                put(id, currentSubGoals)
                                            }

                                            // Progress yüzdesini yeniden hesapla
                                            val updatedSubGoals = currentSubGoals.map { it.second }
                                            val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals)

                                            updateGoalProgress(
                                                goalId = id,
                                                newProgress = newProgress,
                                                onSuccess = {
                                                    // UI'yi güncelle
                                                    if (newProgress < 100) {
                                                        // Hedef artık tamamlanmamış, completed'den active'e taşı
                                                        val goalToUpdate = completedGoals.find { it.first == id }?.second
                                                        if (goalToUpdate != null) {
                                                            val updatedGoal = goalToUpdate.copy(progress = newProgress)
                                                            completedGoals = completedGoals.filter { it.first != id }
                                                            activeGoals = activeGoals + (id to updatedGoal)
                                                        }
                                                    } else {
                                                        // Hedef hala tamamlanmış, progress'i güncelle
                                                        completedGoals = completedGoals.map {
                                                            if (it.first == id) id to it.second.copy(progress = newProgress)
                                                            else it
                                                        }
                                                    }
                                                },
                                                onError = { error ->
                                                    Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                }
                                            )
                                        } else {
                                            Toast.makeText(context, "Alt hedef eklenemedi: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("GoalsScreen", "Unexpected error adding subgoal", e)
                                        Toast.makeText(context, "Beklenmeyen hata: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            // Alt hedef silme
                            onDeleteSubGoal = { subGoal ->
                                coroutineScope.launch {
                                    try {
                                        // Alt hedefi sil
                                        val result = SubGoalUtils.deleteSubGoal(subGoal.id)

                                        if (result.isSuccess) {
                                            Toast.makeText(context, context.getString(R.string.sub_goal_deleted), Toast.LENGTH_SHORT).show()

                                            // UI'yi güncelle
                                            val updatedSubGoals = subGoalsMap[id]?.filter { it.second.id != subGoal.id } ?: emptyList()

                                            // Map'i güncelle
                                            subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                put(id, updatedSubGoals)
                                            }

                                            // Hedefin ilerleme yüzdesini güncelle
                                            val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals.map { it.second })

                                            updateGoalProgress(
                                                goalId = id,
                                                newProgress = newProgress,
                                                onSuccess = {
                                                    // Hedefin durumunu güncelle
                                                    if (newProgress < 100) {
                                                        val goalToUpdate = completedGoals.find { it.first == id }?.second
                                                        if (goalToUpdate != null) {
                                                            val updatedGoal = goalToUpdate.copy(progress = newProgress)
                                                            completedGoals = completedGoals.filter { it.first != id }
                                                            activeGoals = activeGoals + (id to updatedGoal)
                                                        }
                                                    } else {
                                                        // Hedef hala tamamlanmış, progress'i güncelle
                                                        completedGoals = completedGoals.map {
                                                            if (it.first == id) id to it.second.copy(progress = newProgress)
                                                            else it
                                                        }
                                                    }
                                                },
                                                onError = { error ->
                                                    Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                }
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Alt hedef silinemedi: ${result.exceptionOrNull()?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("GoalsScreen", "Error deleting subgoal", e)
                                        Toast.makeText(
                                            context,
                                            "Alt hedef silinirken hata oluştu: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            // Toggle sub goal
                            onToggleSubGoal = { subGoal, isCompleted ->
                                coroutineScope.launch {
                                    try {
                                        // Alt hedefin tamamlanma durumunu güncelle
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

                                            // Map'i güncelle
                                            subGoalsMap = subGoalsMap.toMutableMap().apply {
                                                put(id, updatedSubGoals)
                                            }

                                            // Hedefin ilerleme yüzdesini güncelle
                                            val newProgress = SubGoalUtils.calculateProgressFromSubGoals(updatedSubGoals.map { it.second })

                                            updateGoalProgress(
                                                goalId = id,
                                                newProgress = newProgress,
                                                onSuccess = {
                                                    // UI'yi güncelle
                                                    if (newProgress < 100 && goal.progress >= 100) {
                                                        // Hedef tamamlanmış durumdan çıktı
                                                        completedGoals = completedGoals.filter { it.first != id }
                                                        activeGoals = activeGoals + (id to goal.copy(progress = newProgress))
                                                    } else if (newProgress >= 100 && goal.progress < 100) {
                                                        // Hedef tamamlandı
                                                        activeGoals = activeGoals.filter { it.first != id }
                                                        completedGoals = completedGoals + (id to goal.copy(progress = newProgress))
                                                    } else {
                                                        // Sadece progress güncellendi
                                                        completedGoals = completedGoals.map {
                                                            if (it.first == id) id to it.second.copy(progress = newProgress)
                                                            else it
                                                        }
                                                    }

                                                    Toast.makeText(
                                                        context,
                                                        if (isCompleted) "Alt hedef tamamlandı" else "Alt hedef güncellendi",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                onError = { error ->
                                                    Log.e("GoalsScreen", "Failed to update goal progress: $error")
                                                }
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Alt hedef güncellenemedi: ${result.exceptionOrNull()?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("GoalsScreen", "Error updating subgoal", e)
                                        Toast.makeText(
                                            context,
                                            "Alt hedef güncellenirken hata oluştu: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            // Hedefi silme fonksiyonu
                            onDeleteGoal = {
                                // Hedefi silmek için doğrulama dialog'u göster
                                showDeleteConfirmDialog = id to goal
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

            // Hedef Silme Doğrulama Dialog'u
            showDeleteConfirmDialog?.let { (goalId, goal) ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text("Hedefi Sil") },
                    text = { Text("\"${goal.title}\" hedefini silmek istediğinize emin misiniz? Bu işlem geri alınamaz ve tüm alt hedefler de silinecektir.") },
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
                            )
                        ) {
                            Text("Evet, Sil")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = null }) {
                            Text("İptal")
                        }
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
                    label = { Text(stringResource(R.string.goal_title)) },
                    placeholder = { Text(stringResource(R.string.sub_goal_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // İlerleme Slider
                Text(
                    text = stringResource(R.string.current_progress, goalProgress),
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
                Text(stringResource(R.string.save))
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


