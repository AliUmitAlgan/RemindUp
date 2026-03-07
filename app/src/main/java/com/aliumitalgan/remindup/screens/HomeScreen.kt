
package com.aliumitalgan.remindup.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.components.*
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.ui.theme.AppColors
import com.aliumitalgan.remindup.utils.LanguageManager
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    onNavigateToGoals: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAssistant: () -> Unit = {}
) {
    val currentLanguage by LanguageManager.currentLanguage
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var motivationalMessage by remember { mutableStateOf("") }
    var overallProgress by remember { mutableStateOf(0f) }

    // Current time and date
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
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
        BottomNavItem(stringResource(R.string.home), Icons.Filled.Home, Icons.Outlined.Home, "home"),
        BottomNavItem(stringResource(R.string.goals), Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "goals"),
        BottomNavItem(stringResource(R.string.reminders), Icons.Filled.Notifications, Icons.Outlined.Notifications, "reminders"),
        BottomNavItem(stringResource(R.string.progress), Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "progress"),
        BottomNavItem(stringResource(R.string.profile), Icons.Filled.Person, Icons.Outlined.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[0].route) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayName = currentUser?.displayName ?: currentUser?.email?.substringBefore('@') ?: "Misafir"

    // Load data
    LaunchedEffect(key1 = Unit, key2 = currentLanguage) {
        try {
            // Get motivational message
            motivationalMessage = ReminderUtils.getRandomMotivationalMessage(context)

            // Load goals
            val goalsResult = ProgressUtils.getUserGoals()
            if (goalsResult.isSuccess) {
                goals = goalsResult.getOrDefault(emptyList()).map { it.second }
            }

            // Load reminders
            val remindersResult = ReminderUtils.getUserReminders()
            if (remindersResult.isSuccess) {
                reminders = remindersResult.getOrDefault(emptyList()).map { it.second }
                // Sort reminders by time
                reminders = reminders.sortedBy { it.time }
            }

            // Get overall progress
            val progressResult = ProgressUtils.getOverallProgress()
            if (progressResult.isSuccess) {
                overallProgress = progressResult.getOrDefault(0f)
            }

            isLoading = false
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error loading data: ${e.message}", e)
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                currentRoute = selectedNavItem,
                onItemSelected = { route ->
                    selectedNavItem = route
                    when (route) {
                        "home" -> {} // Already on home
                        "goals" -> onNavigateToGoals()
                        "reminders" -> onNavigateToReminders()
                        "progress" -> onNavigateToProgress()
                        "profile" -> onNavigateToSettings()
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedFloatingActionButton(
                onClick = {
                    // Determine where to navigate based on the user's context
                    if (goals.isEmpty()) {
                        onNavigateToGoals()
                    } else if (reminders.isEmpty()) {
                        onNavigateToReminders()
                    } else {
                        // Show a menu or dialog to choose where to navigate
                        onNavigateToGoals()
                    }
                },
                icon = Icons.Filled.Add
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                // Loading state with animation
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Main content with scroll
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Modern top profile section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.welcome_user),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )

                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onNavigateToAssistant,
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Assistant",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    if (reminders.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Date & Time chip
                    EnhancedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        cornerRadius = 16.dp,
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dateFormat.format(Date(currentTime.value)),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = timeFormat.format(Date(currentTime.value)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Motivational card
                    HeaderCard(
                        title = stringResource(R.string.user_greeting),
                        subtitle = motivationalMessage,
                        icon = Icons.Default.EmojiEmotions,
                        primaryColor = MaterialTheme.colorScheme.tertiary
                    )

                    // Progress summary card
                    ProgressSummaryCard(
                        progress = overallProgress,
                        onCardClick = onNavigateToProgress
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Upcoming Reminders Section
                    SectionHeader(
                        title = stringResource(R.string.upcoming_reminders),
                        actionButton = {
                            TextButton(onClick = onNavigateToReminders) {
                                Text(stringResource(R.string.see_all))
                            }
                        }
                    )

                    // Top 3 upcoming reminders or empty state
                    if (reminders.isNotEmpty()) {
                        val upcomingReminders = reminders.filter { it.isEnabled }.take(3)

                        upcomingReminders.forEach { reminder ->
                            ReminderListItem(
                                reminder = reminder,
                                onClick = onNavigateToReminders
                            )
                        }
                    } else {
                        EmptyStateCard(
                            title = stringResource(R.string.no_reminders),
                            subtitle = stringResource(R.string.add_first_reminder),
                            icon = Icons.Default.Notifications,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            onActionClick = onNavigateToReminders,
                            actionText = stringResource(R.string.add_reminder)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Active Goals Section
                    SectionHeader(
                        title = stringResource(R.string.active_goals),
                        actionButton = {
                            TextButton(onClick = onNavigateToGoals) {
                                Text(stringResource(R.string.see_all))
                            }
                        }
                    )

                    // Top 3 active goals or empty state
                    if (goals.isNotEmpty()) {
                        val activeGoals = goals.filter { it.progress < 100 }.take(3)

                        if (activeGoals.isNotEmpty()) {
                            activeGoals.forEach { goal ->
                                GoalListItem(
                                    goal = goal,
                                    onCardClick = onNavigateToGoals
                                )
                            }
                        } else {
                            // All goals are completed
                            AllGoalsCompletedCard(onActionClick = onNavigateToGoals)
                        }
                    } else {
                        EmptyStateCard(
                            title = stringResource(R.string.no_goals),
                            subtitle = stringResource(R.string.add_first_goal),
                            icon = Icons.Default.Flag,
                            primaryColor = MaterialTheme.colorScheme.secondary,
                            onActionClick = onNavigateToGoals,
                            actionText = stringResource(R.string.add_goal)
                        )
                    }

                    // Extra space at bottom for FAB
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ProgressSummaryCard(
    progress: Float,
    onCardClick: () -> Unit
) {
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    EnhancedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 8.dp)
            .clickable(onClick = onCardClick),
        cornerRadius = 24.dp,
        elevation = 4.dp,
        primaryColor = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress.value },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        strokeWidth = 12.dp
                    )

                    // Center text showing percentage
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(animatedProgress.value * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = stringResource(R.string.completed_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Right side text
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.overall_progress),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.goals_status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Call to action button
                    EnhancedButton(
                        text = stringResource(R.string.see_all),
                        onClick = onCardClick,
                        icon = Icons.Default.ShowChart,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderListItem(
    reminder: Reminder,
    onClick: () -> Unit
) {
    // Get colors based on category
    val categoryColor = getCategoryColor(reminder.category)

    EnhancedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        cornerRadius = 16.dp,
        primaryColor = categoryColor,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reminder.time,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and optional description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Category indicator
            Icon(
                imageVector = getCategoryIcon(reminder.category),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun GoalListItem(
    goal: Goal,
    onCardClick: () -> Unit
) {
    val animatedProgress = animateFloatAsState(
        targetValue = goal.progress / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "goalProgress"
    )

    val progressColor = when {
        goal.progress >= 100 -> MaterialTheme.colorScheme.tertiary
        goal.progress >= 75 -> MaterialTheme.colorScheme.secondary
        goal.progress >= 50 -> MaterialTheme.colorScheme.primary
        goal.progress >= 25 -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    EnhancedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onCardClick),
        cornerRadius = 16.dp,
        primaryColor = progressColor,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
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
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    primaryColor: Color,
    onActionClick: () -> Unit,
    actionText: String
) {
    var isAnimating by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.1f else 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyScale"
    )

    LaunchedEffect(Unit) {
        isAnimating = true
    }

    EnhancedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 16.dp,
        primaryColor = primaryColor,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            EnhancedButton(
                text = actionText,
                onClick = onActionClick,
                icon = Icons.Default.Add,
                backgroundColor = primaryColor
            )
        }
    }
}

@Composable
fun AllGoalsCompletedCard(onActionClick: () -> Unit) {
    EnhancedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 16.dp,
        primaryColor = AppColors.success,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppColors.success.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = AppColors.success,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.congratulations),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tüm hedeflerinizi tamamladınız!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            EnhancedButton(
                text = "Yeni Hedef Ekle",
                onClick = onActionClick,
                icon = Icons.Default.Add,
                backgroundColor = AppColors.success
            )
        }
    }
}

// Helper function to get category color
@Composable
private fun getCategoryColor(category: ReminderCategory): Color {
    return when (category) {
        ReminderCategory.GENERAL -> MaterialTheme.colorScheme.primary
        ReminderCategory.WORK -> Color(0xFFE91E63)     // Pink
        ReminderCategory.HEALTH -> Color(0xFFE53935)   // Red
        ReminderCategory.PERSONAL -> Color(0xFF9C27B0) // Purple
        ReminderCategory.STUDY -> Color(0xFF00897B)    // Teal
        ReminderCategory.FITNESS -> Color(0xFF7CB342)  // Green
    }
}

// Helper function to get category icon
private fun getCategoryIcon(category: ReminderCategory): ImageVector {
    return when (category) {
        ReminderCategory.GENERAL -> Icons.Default.Notifications
        ReminderCategory.WORK -> Icons.Default.Work
        ReminderCategory.HEALTH -> Icons.Default.Favorite
        ReminderCategory.PERSONAL -> Icons.Default.Person
        ReminderCategory.STUDY -> Icons.Default.School
        ReminderCategory.FITNESS -> Icons.Default.FitnessCenter
    }
}
