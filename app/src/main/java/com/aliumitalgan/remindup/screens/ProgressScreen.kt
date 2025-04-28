package com.aliumitalgan.remindup.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.components.ProgressBar
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.utils.AnimationUtils
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreenContent(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var overallProgress by remember { mutableStateOf(0f) }
    var goals by remember { mutableStateOf<List<Pair<String, Goal>>>(emptyList()) }
    var completedGoalsCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Verileri yükle
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            // Genel ilerlemeyi hesapla
            val progressResult = ProgressUtils.getOverallProgress()
            if (progressResult.isSuccess) {
                overallProgress = progressResult.getOrDefault(0f)
            }

            // Hedefleri getir
            val goalsResult = ProgressUtils.getUserGoals()
            if (goalsResult.isSuccess) {
                goals = goalsResult.getOrDefault(emptyList())
            }

            // Tamamlanan hedef sayısını getir
            val completedResult = ProgressUtils.getCompletedGoalsCount()
            if (completedResult.isSuccess) {
                completedGoalsCount = completedResult.getOrDefault(0)
            }

            isLoading = false
        }
    }

    // Animasyonlu ilerleme değeri
    val animatedProgress = animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlerleme Durumu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Motivasyon mesajı
                    item {
                        val motivationalMessage = remember {
                            ReminderUtils.getRandomMotivationalMessage()
                        }

                        AnimationUtils.FadeAnimation(visible = true) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = motivationalMessage,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Genel ilerleme
                    item {
                        AnimationUtils.FadeAnimation(visible = true) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Genel İlerleme",
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Daire şeklinde ilerleme göstergesi
                                    Box(
                                        modifier = Modifier.size(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = animatedProgress.value,
                                            modifier = Modifier.size(200.dp),
                                            strokeWidth = 12.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text(
                                            text = "${(animatedProgress.value * 100).toInt()}%",
                                            style = MaterialTheme.typography.headlineLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Tamamlanan Hedefler: $completedGoalsCount",
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Toplam Hedefler: ${goals.size}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Hedefler başlığı
                    item {
                        Text(
                            text = "Hedeflerinizin Durumu",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    // Hedefler listesi
                    if (goals.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Henüz hedef eklenmemiş",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    } else {
                        items(goals) { (id, goal) ->
                            GoalProgressItem(goal = goal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalProgressItem(goal: Goal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
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

                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // İlerleme göstergesi
            AnimationUtils.ProgressAnimation(targetValue = goal.progress / 100f) { animatedProgress ->
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        goal.progress < 30 -> MaterialTheme.colorScheme.error
                        goal.progress < 70 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Hedef %100 tamamlandıysa kutlama mesajı
            if (goal.progress >= 100) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = "Tamamlandı",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Tamamlandı!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Toast mesajı göster
private fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}