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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.data.repository.FriendAchiever
import com.aliumitalgan.remindup.data.repository.SocialData
import com.aliumitalgan.remindup.data.repository.SocialRepository
import com.aliumitalgan.remindup.ui.theme.appCardColor
import com.aliumitalgan.remindup.ui.theme.appTextPrimary
import com.aliumitalgan.remindup.ui.theme.appTextSecondary
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.ProgressUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val AccentOrange = Color(0xFFF26522)
private val LightBg: Color
    get() = themedColor(Color(0xFFFBF8F4), Color(0xFF0F131A))

@Composable
fun SocialScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProgress: () -> Unit = {},
    onAddFriend: () -> Unit = {}
) {
    val repository = remember { SocialRepository() }
    var refreshKey by remember { mutableStateOf(0) }
    val socialFlow = remember(refreshKey) { repository.getSocialData() }
    val result by socialFlow.collectAsState(initial = null)
    val navItems = mainBottomNavItems()

    LaunchedEffect(Unit) {
        val goals = withContext(Dispatchers.IO) {
            ProgressUtils.getUserGoals().getOrDefault(emptyList())
        }
        val completed = goals.count { it.second.progress >= 100 }
        val total = goals.size
        val progress = if (total > 0) (completed * 100) / total else 0
        repository.syncCurrentUserStats(progress)
    }

    Scaffold(
        containerColor = LightBg,
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = "social",
                onItemSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> onNavigateToGoals()
                        "social" -> Unit
                        "analytic" -> onNavigateToProgress()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = appTextPrimary
                    )
                }
                Text(
                    "Friends Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appTextPrimary
                )
                IconButton(onClick = onAddFriend) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        contentDescription = "Add Friend",
                        tint = AccentOrange
                    )
                }
            }

            when {
                result == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentOrange)
                    }
                }
                result?.isSuccess == true -> {
                    val data = result?.getOrNull()
                    if (data != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                DailyMomentumSection(
                                    avgProgress = data.avgGroupProgress,
                                    friendCount = data.friendCount
                                )
                            }
                            item {
                                Text(
                                    "TOP ACHIEVERS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = appTextSecondary
                                )
                            }
                            items(data.achievers) { achiever ->
                                AchieverCard(achiever = achiever)
                            }
                            if (data.achievers.isEmpty()) {
                                item {
                                    EmptySocialState()
                                }
                            }
                        }
                    }
                }
                else -> {
                    ErrorSocialState(
                        message = result?.exceptionOrNull()?.message ?: "Unable to load social data.",
                        onRetry = { refreshKey += 1 }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySocialState() {
    Surface(
        color = appCardColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No friend activity yet",
                fontWeight = FontWeight.Bold,
                color = appTextPrimary,
                fontSize = 16.sp
            )
            Text(
                text = "When friends connect and update progress, you'll see them here.",
                color = appTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ErrorSocialState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appCardColor,
            onClick = onRetry
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Social feed unavailable",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = appTextPrimary
                )
                Text(
                    text = message,
                    color = appTextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "Tap to retry",
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DailyMomentumSection(
    avgProgress: Int,
    friendCount: Int
) {
    Surface(
        color = appCardColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.height(48.dp)) {
                listOf(
                    Color(0xFFFFD7C4),
                    Color(0xFFE8F5E9),
                    Color(0xFFE3F2FD),
                    Color(0xFFF3E5F5)
                ).take(4).forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (index * 32).dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = appTextPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (4 * 32).dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+$friendCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$avgProgress%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange
                )
                Text(
                    text = "AVG GROUP",
                    fontSize = 12.sp,
                    color = appTextSecondary
                )
            }
        }
    }
}

@Composable
private fun AchieverCard(achiever: FriendAchiever) {
    Surface(
        color = appCardColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD7C4)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achiever.displayName.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = appTextPrimary
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = achiever.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appTextPrimary
                    )
                    achiever.rank?.let { rank ->
                        Spacer(modifier = Modifier.size(8.dp))
                        Surface(
                            color = AccentOrange,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Rank $rank",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = achiever.status,
                    fontSize = 12.sp,
                    color = if (achiever.status == "CRUSHING IT!") AccentOrange else appTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(themedColor(Color(0xFFE5E7EB), Color(0xFF334155)))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(achiever.progressPercent / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(AccentOrange)
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            if (achiever.hasStreak) {
                Icon(
                    Icons.Filled.Whatshot,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
            Text(
                text = "${achiever.progressPercent}%",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = appTextPrimary
            )
        }
    }
}

