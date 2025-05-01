package com.aliumitalgan.remindup.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.aliumitalgan.remindup.R
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.GoalCard
import com.aliumitalgan.remindup.components.ModernCard
import com.aliumitalgan.remindup.components.MotivationalMessage
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.ui.theme.*
import com.aliumitalgan.remindup.utils.LanguageManager
import com.aliumitalgan.remindup.utils.ReminderUtils
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    onNavigateToGoals: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentLanguage by LanguageManager.currentLanguage
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var motivationalMessage by remember { mutableStateOf("") }

    // Current time and date
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    // Update time every minute
    LaunchedEffect(Unit) {
        while(true) {
            currentTime.value = System.currentTimeMillis()
            delay(60000) // Update every minute
        }
    }

    // Bottom Navigation Items
    val bottomNavItems = listOf(
        BottomNavItem("Ana Sayfa", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        BottomNavItem("Hedefler", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "goals"),
        BottomNavItem("Hatırlatıcılar", Icons.Filled.Notifications, Icons.Outlined.Notifications, "reminders"),
        BottomNavItem("İlerleme", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "progress"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[0].route) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayName = currentUser?.displayName ?: currentUser?.email?.substringBefore('@') ?: "Misafir"

    // Verileri yükle
    LaunchedEffect(key1 = true,key2=currentLanguage) {
        try {
            // Context'i al
            val appContext = context

            // Motivasyonel mesaj - Şimdi context ile çağırılıyor
            motivationalMessage = ReminderUtils.getRandomMotivationalMessage(context)

            // Hedefleri yükle
            com.aliumitalgan.remindup.utils.FirebaseUtils.getGoals { goalsList ->
                goals = goalsList
                isLoading = false
            }

            // Hatırlatıcıları yükle
            val remindersResult = ReminderUtils.getUserReminders()
            if (remindersResult.isSuccess) {
                reminders = remindersResult.getOrDefault(emptyList()).map { it.second }
            }
        } catch (e: Exception) {
            // Hata durumunda yüklemeyi durdurun
            isLoading = false
            // Hata mesajını göster
            Toast.makeText(context, "Veri yüklenirken bir hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Icon with animation
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            BluePrimary,
                                            GreenSecondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "RemindUp",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = dateFormat.format(Date(currentTime.value)),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                currentRoute = selectedNavItem,
                onItemSelected = { route ->
                    // Şu anki route ile aynı route'a tıklanırsa bir şey yapma
                    if (route != selectedNavItem) {
                        selectedNavItem = route
                        when (route) {
                            "home" -> {}  // Zaten ana sayfadayız
                            "goals" -> onNavigateToGoals()
                            "reminders" -> onNavigateToReminders()
                            "progress" -> onNavigateToProgress()
                            "profile" -> onNavigateToSettings()
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    MaterialTheme.colorScheme.background
                )
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else {
                HomeContent(
                    displayName = displayName,
                    motivationalMessage = motivationalMessage,
                    goals = goals,
                    reminders = reminders,
                    onNavigateToGoals = onNavigateToGoals,
                    onNavigateToReminders = onNavigateToReminders
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .size(60.dp * scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun HomeContent(
    displayName: String,
    motivationalMessage: String,
    goals: List<Goal>,
    reminders: List<Reminder>,
    onNavigateToGoals: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Kullanıcı karşılama mesajı
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInHorizontally(
                            animationSpec = tween(1000),
                            initialOffsetX = { -it }
                        )
            ) {
                WelcomeCard(displayName)
            }
        }

        // Motivasyon mesajı
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 300))
            ) {
                MotivationalMessage(message = motivationalMessage)
            }
        }

        // Hedefler Başlık
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 600))
            ) {
                SectionTitle(stringResource(R.string.goals), stringResource(R.string.see_all)) {
                    onNavigateToGoals()
                }
            }
        }

        // Hedefler
        if (goals.isEmpty()) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 900))
                ) {
                    EmptyStateCard(
                        stringResource(R.string.no_goals),
                        stringResource(R.string.add_first_goal),
                        stringResource(R.string.add_goal),
                        onClick = onNavigateToGoals
                    )
                }
            }
        } else {
            items(goals.take(3)) { goal ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 900)) +
                            slideInHorizontally(
                                animationSpec = tween(1000, delayMillis = 900),
                                initialOffsetX = { it }
                            )
                ) {
                    GoalCard(
                        goalTitle = goal.title,
                        goalProgress = goal.progress
                    )
                }
            }
        }

        // Yaklaşan Hatırlatıcılar Başlık
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 1200))
            ) {
                SectionTitle(stringResource(R.string.upcoming_reminders), stringResource(R.string.see_all)) {
                    onNavigateToReminders()
                }
            }
        }

        // Yaklaşan Hatırlatıcılar
        if (reminders.isEmpty()) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 1500))
                ) {
                    EmptyStateCard(
                        title = "Henüz hatırlatıcı eklenmemiş",
                        description = "İlk hatırlatıcını ekleyerek başla",
                        buttonText = "Hatırlatıcı Ekle",
                        onClick = onNavigateToReminders
                    )
                }
            }
        } else {
            items(reminders.take(3)) { reminder ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 1500)) +
                            slideInHorizontally(
                                animationSpec = tween(1000, delayMillis = 1500),
                                initialOffsetX = { it }
                            )
                ) {
                    ReminderListItem(reminder)
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(userName: String) {
    ModernCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        // Boş string kontrolü
                        text = if (userName.isNotEmpty()) userName.first().toString().uppercase() else "?",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        stringResource(R.string.welcome_user),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        // Boş userName durumunda varsayılan değer
                        text = if (userName.isNotEmpty()) userName else "Misafir",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        stringResource(R.string.user_greeting),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, actionText: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        TextButton(onClick = onAction) {
            Text(
                text = actionText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun ReminderListItem(reminder: Reminder) {
    val currentLanguage by LanguageManager.currentLanguage

    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Saat ikonu
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                BluePrimary.copy(alpha = 0.2f),
                                GreenSecondary.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Saat",
                    tint = BluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Saat bilgisini dil desteği ile göster
                Text(
                    text = if (currentLanguage == LanguageManager.LANGUAGE_ENGLISH) {
                        "Time: ${reminder.time}"
                    } else {
                        "Saat: ${reminder.time}"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}