package com.aliumitalgan.remindup.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.components.GoalCard
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.utils.AnimationUtils
import com.aliumitalgan.remindup.utils.FirebaseUtils
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreenContent(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var goals by remember { mutableStateOf<List<Pair<String, Goal>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Pair<String, Goal>?>(null) }

    // Hedefleri yükle
    LaunchedEffect(key1 = true) {
        loadGoals(
            onSuccess = { goalsList ->
                goals = goalsList
                isLoading = false
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
                title = { Text("Hedeflerim") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Hedef Ekle"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (goals.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Henüz hedef bulunmuyor.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Eklemek için + butonuna tıklayın.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goals) { (id, goal) ->
                        AnimationUtils.SlideAnimation(visible = true) {
                            GoalItem(
                                goalId = id,
                                goal = goal,
                                onEdit = { editingGoal = id to goal },
                                onDelete = {
                                    coroutineScope.launch {
                                        deleteGoal(
                                            goalId = id,
                                            onSuccess = {
                                                goals = goals.filter { it.first != id }
                                                showToast(context, "Hedef silindi")
                                            },
                                            onError = { error ->
                                                showToast(context, "Hedef silinemedi: $error")
                                            }
                                        )
                                    }
                                },
                                onProgressChange = { newProgress ->
                                    coroutineScope.launch {
                                        updateGoalProgress(
                                            goalId = id,
                                            newProgress = newProgress,
                                            onSuccess = {
                                                // Hedefi güncelle
                                                goals = goals.map { pair ->
                                                    if (pair.first == id) {
                                                        id to pair.second.copy(progress = newProgress)
                                                    } else {
                                                        pair
                                                    }
                                                }

                                                if (newProgress >= 100) {
                                                    showToast(context, "Tebrikler! Hedefinizi tamamladınız!")
                                                } else {
                                                    showToast(context, "İlerleme güncellendi")
                                                }
                                            },
                                            onError = { error ->
                                                showToast(context, "İlerleme güncellenemedi: $error")
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Hedef ekleme diyaloğu
            if (showAddDialog) {
                GoalDialog(
                    title = "Hedef Ekle",
                    initialGoal = Goal("", 0),
                    onDismiss = { showAddDialog = false },
                    onConfirm = { title, progress ->
                        coroutineScope.launch {
                            val newGoal = Goal(title = title, progress = progress)
                            addGoal(
                                goal = newGoal,
                                onSuccess = { id ->
                                    goals = goals + (id to newGoal)
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

            // Hedef düzenleme diyaloğu
            editingGoal?.let { (id, goal) ->
                GoalDialog(
                    title = "Hedefi Düzenle",
                    initialGoal = goal,
                    onDismiss = { editingGoal = null },
                    onConfirm = { title, progress ->
                        coroutineScope.launch {
                            val updatedGoal = Goal(title = title, progress = progress)
                            updateGoal(
                                goalId = id,
                                goal = updatedGoal,
                                onSuccess = {
                                    goals = goals.map { pair ->
                                        if (pair.first == id) {
                                            id to updatedGoal
                                        } else {
                                            pair
                                        }
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
fun GoalItem(
    goalId: String,
    goal: Goal,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onProgressChange: (Int) -> Unit
) {
    var showProgressDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    // Düzenle butonu
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                    }

                    // Sil butonu
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // İlerleme animasyonu
            AnimationUtils.ProgressAnimation(targetValue = goal.progress / 100f) { animatedProgress ->
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goal.progress}% Tamamlandı",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = { showProgressDialog = true },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("İlerleme Güncelle")
                }
            }

            // Hedef %100 tamamlandıysa kutlama mesajı
            if (goal.progress >= 100) {
                AnimationUtils.GoalCompletedAnimation(isCompleted = true) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Tebrikler! Bu hedefi tamamladınız!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    // İlerleme güncelleme diyaloğu
    if (showProgressDialog) {
        ProgressUpdateDialog(
            currentProgress = goal.progress,
            onDismiss = { showProgressDialog = false },
            onConfirm = {
                onProgressChange(it)
                showProgressDialog = false
            }
        )
    }
}

@Composable
fun GoalDialog(
    title: String,
    initialGoal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (title: String, progress: Int) -> Unit
) {
    var goalTitle by remember { mutableStateOf(initialGoal.title) }
    var goalProgress by remember { mutableStateOf(initialGoal.progress.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("Hedef Başlığı") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = goalProgress,
                    onValueChange = { input ->
                        // Sadece sayısal değerleri kabul et
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            val num = input.toIntOrNull() ?: 0
                            if (num in 0..100) {
                                goalProgress = input
                            }
                        }
                    },
                    label = { Text("İlerleme (%)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (goalTitle.isNotEmpty()) {
                        onConfirm(
                            goalTitle,
                            goalProgress.toIntOrNull() ?: 0
                        )
                    }
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun ProgressUpdateDialog(
    currentProgress: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var progress by remember { mutableStateOf(currentProgress.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("İlerleme Güncelle") },
        text = {
            Column {
                Text("Mevcut ilerleme: $currentProgress%")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = progress,
                    onValueChange = { input ->
                        // Sadece sayısal değerleri kabul et
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            val num = input.toIntOrNull() ?: 0
                            if (num in 0..100) {
                                progress = input
                            }
                        }
                    },
                    label = { Text("Yeni İlerleme (%)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(progress.toIntOrNull() ?: currentProgress)
                }
            ) {
                Text("Güncelle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
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

// Toast mesajı göster
private fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}