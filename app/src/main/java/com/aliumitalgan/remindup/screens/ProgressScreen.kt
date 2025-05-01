package com.aliumitalgan.remindup.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.GoalCard
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.utils.LanguageManager
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var overallProgress by remember { mutableStateOf(0f) }
    var goals by remember { mutableStateOf<List<Pair<String, Goal>>>(emptyList()) }
    var completedGoalsCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showAllGoals by remember { mutableStateOf(false) }

    // Güncel dil durumunu takip et
    val currentLanguage by LanguageManager.currentLanguage

    // Motivasyon mesajını dil durumuna göre al
    val motivationalMessage = remember(currentLanguage) {
        ReminderUtils.getRandomMotivationalMessage(context)
    }

    // Dil değişikliğini logla
    LaunchedEffect(currentLanguage) {
        Log.d("ProgressScreen", "Current language for motivational messages: $currentLanguage")
    }

    // Bottom Navigation Items
    val bottomNavItems = listOf(
        BottomNavItem("Ana Sayfa", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        BottomNavItem("Hedefler", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "goals"),
        BottomNavItem("Hatırlatıcılar", Icons.Filled.Notifications, Icons.Outlined.Notifications, "reminders"),
        BottomNavItem("İlerleme", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "progress"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[3].route) }

    // Verileri yükle
    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            try {
                // Genel ilerleme
                val progressResult = ProgressUtils.getOverallProgress()
                if (progressResult.isSuccess) {
                    overallProgress = progressResult.getOrDefault(0f)
                }

                // Tamamlanan hedef sayısı
                val completedResult = ProgressUtils.getCompletedGoalsCount()
                if (completedResult.isSuccess) {
                    completedGoalsCount = completedResult.getOrDefault(0)
                }

                // Tüm hedefleri getir
                val goalsResult = ProgressUtils.getUserGoals()
                if (goalsResult.isSuccess) {
                    goals = goalsResult.getOrDefault(emptyList())
                }

                isLoading = false
            } catch (e: Exception) {
                Toast.makeText(context, "Veriler yüklenirken hata: ${e.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.progress)) },
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
                        "goals" -> onNavigateToGoals()
                        "reminders" -> onNavigateToReminders()
                        "progress" -> {} // Zaten progress ekranındayız
                        "profile" -> onNavigateToSettings()
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
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Motivasyon mesajı - ReminderUtils ile alınıyor (dil duyarlı)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = motivationalMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Genel İlerleme
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.overall_progress),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Dairesel ilerleme göstergesi
                            Box(
                                modifier = Modifier.size(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Arka plan daire
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.size(200.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 16.dp
                                )

                                // İlerleme dairesi
                                CircularProgressIndicator(
                                    progress = { overallProgress },
                                    modifier = Modifier.size(200.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 16.dp
                                )

                                // İç içe daire
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${(overallProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text(
                                            stringResource(R.string.completed_label),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // İstatistikler
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Toplam Hedefler
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${goals.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        stringResource(R.string.total_goals),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                // Tamamlanan Hedefler
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$completedGoalsCount",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        stringResource(R.string.completed_goals),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Hedef Başlığı
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.goals_status),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(onClick = { showAllGoals = !showAllGoals }) {
                            Text(
                                if (showAllGoals) stringResource(R.string.brief) else stringResource(R.string.see_all),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Hedef listesi - gösterim şeklini değiştir
                if (!showAllGoals) {
                    // İlk 3 hedefi göster
                    items(goals.take(3)){ (id, goal) ->
                        GoalCard(
                            goalTitle = goal.title,
                            goalProgress = goal.progress
                        )
                    }
                } else {
                    // Tüm hedefleri göster - kategorize edilen liste
                    val activeGoals = goals.filter { it.second.progress < 100 }
                    val completedGoals = goals.filter { it.second.progress >= 100 }

                    // Aktif Hedefler Başlığı
                    if (activeGoals.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.active_goals),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }

                    // Aktif Hedefler
                    items(activeGoals) { (id, goal) ->
                        GoalCard(
                            goalTitle = goal.title,
                            goalProgress = goal.progress
                        )
                    }

                    // Tamamlanmış Hedefler Başlığı
                    if (completedGoals.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.completed_goals),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }

                    // Tamamlanmış Hedefler
                    items(completedGoals) { (id, goal) ->
                        GoalCard(
                            goalTitle = goal.title,
                            goalProgress = goal.progress
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ModernProgressItem(goal: Goal) {
    val progressColor = when {
        goal.progress >= 100 -> MaterialTheme.colorScheme.primary
        goal.progress >= 75 -> MaterialTheme.colorScheme.primary
        goal.progress >= 50 -> MaterialTheme.colorScheme.tertiary
        goal.progress >= 25 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
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
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Özel tasarımlı ilerleme çubuğu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.progress / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
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

            // Tamamlandı işareti
            if (goal.progress >= 100) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Tamamlandı",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(R.string.completed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// Toast mesajı göster
fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}