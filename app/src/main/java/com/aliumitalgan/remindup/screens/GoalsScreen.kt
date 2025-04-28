package com.aliumitalgan.remindup.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.models.Goal
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
    var showProgressDialog by remember { mutableStateOf<Pair<String, Goal>?>(null) }

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
            } else if (goals.isEmpty()) {
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
                    items(goals.size) { index ->
                        val (id, goal) = goals[index]
                        ModernGoalListItem(
                            goal = goal,
                            onEditClick = { editingGoal = id to goal },
                            onDeleteClick = {
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
                            onProgressUpdate = { showProgressDialog = id to goal }
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

            // İlerleme Güncelleme Diyaloğu
            showProgressDialog?.let { (id, goal) ->
                ProgressUpdateDialog(
                    currentProgress = goal.progress,
                    onDismiss = { showProgressDialog = null },
                    onConfirm = { newProgress ->
                        coroutineScope.launch {
                            updateGoalProgress(
                                goalId = id,
                                newProgress = newProgress,
                                onSuccess = {
                                    goals = goals.map { pair ->
                                        if (pair.first == id) {
                                            id to goal.copy(progress = newProgress)
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
                        showProgressDialog = null
                    }
                )
            }
        }
    }
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
            Text("Hedef Ekle")
        }
    }
}

@Composable
fun ModernGoalListItem(
    goal: Goal,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onProgressUpdate: () -> Unit
) {
    val progressColor = when {
        goal.progress >= 100 -> MaterialTheme.colorScheme.primary
        goal.progress >= 70 -> MaterialTheme.colorScheme.secondary
        goal.progress >= 30 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    // Düzenleme butonu
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Silme butonu
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İlerleme çubuğu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.progress / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    progressColor,
                                    progressColor.copy(alpha = 0.7f)
                                )
                            )
                        )
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
                    onClick = onProgressUpdate,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = progressColor
                    )
                ) {
                    Text("İlerleme Güncelle")
                }
            }

            // Hedef tamamlandı işareti
            if (goal.progress >= 100) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Tamamlandı",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Tebrikler! Bu hedefi tamamladınız!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ModernGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, progress: Int) -> Unit,
    title: String = "Hedef Ekle",
    initialTitle: String = "",
    initialProgress: Int = 0
) {
    var goalTitle by remember { mutableStateOf(initialTitle) }
    var goalProgress by remember { mutableStateOf(initialProgress.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        icon = {
            Icon(Icons.Default.Flag, contentDescription = null)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("Hedef Başlığı") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "İlerleme (%)",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = goalProgress,
                    onValueChange = {
                        if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..100)) {
                            goalProgress = it
                        }
                    },
                    label = { Text("İlerleme (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.ShowChart, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // İlerleme slider'ı
                Slider(
                    value = goalProgress.toFloatOrNull() ?: 0f,
                    onValueChange = { goalProgress = it.toInt().toString() },
                    valueRange = 0f..100f,
                    steps = 100,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (goalTitle.isNotEmpty()) {
                        onSave(goalTitle, goalProgress.toIntOrNull() ?: 0)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
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