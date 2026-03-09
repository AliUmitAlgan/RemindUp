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
import androidx.compose.material3.MaterialTheme
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
import com.aliumitalgan.remindup.utils.ProgressUtils

private val AccentOrange = Color(0xFFF26522)
private val LightBg = Color(0xFFFBF8F4)

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
    val socialFlow = remember { repository.getSocialData() }
    val result by socialFlow.collectAsState(initial = Result.failure(Exception("Loading...")))
    var currentRoute by remember { mutableStateOf("social") }
    val navItems = mainBottomNavItems()

    LaunchedEffect(Unit) {
        val goals = ProgressUtils.getUserGoals().getOrDefault(emptyList())
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
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> onNavigateToGoals()
                        "social" -> Unit
                        "analytic" -> onNavigateToProgress()
                        "settings" -> onNavigateToSettings()
                    }
                },
                onCenterActionClick = onNavigateToGoals
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color(0xFF1A1A1A)
                    )
                }
                Text(
                    "Friends Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A1A)
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
                result.isSuccess -> {
                    val data = result.getOrNull()
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
                                    color = Color(0xFF6B7280)
                                )
                            }
                            items(data.achievers.ifEmpty {
                                listOf(
                                    FriendAchiever("1", "Ali Henderson", null, 92, "CRUSHING IT!", 1, false),
                                    FriendAchiever("2", "Sarah Chen", null, 85, "Almost there", null, true),
                                    FriendAchiever("3", "Michael Ross", null, 64, "Steady progress", null, false),
                                    FriendAchiever("4", "Elena Gilbert", null, 32, "Starting the day", null, false)
                                )
                            }) { achiever ->
                                AchieverCard(achiever = achiever)
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                DailyMomentumSection(avgProgress = 85, friendCount = 16)
                            }
                            item {
                                Text(
                                    "TOP ACHIEVERS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            items(
                                listOf(
                                    FriendAchiever("1", "Ali Henderson", null, 92, "CRUSHING IT!", 1, false),
                                    FriendAchiever("2", "Sarah Chen", null, 85, "Almost there", null, true),
                                    FriendAchiever("3", "Michael Ross", null, 64, "Steady progress", null, false),
                                    FriendAchiever("4", "Elena Gilbert", null, 32, "Starting the day", null, false)
                                )
                            ) { achiever ->
                                AchieverCard(achiever = achiever)
                            }
                        }
                    }
                }
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
        color = Color.White,
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
                            color = Color(0xFF1A1A1A)
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
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun AchieverCard(achiever: FriendAchiever) {
    Surface(
        color = Color.White,
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
                    color = Color(0xFF1A1A1A)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = achiever.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A1A)
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
                    color = if (achiever.status == "CRUSHING IT!") AccentOrange else Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE5E7EB))
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
                color = Color(0xFF1A1A1A)
            )
        }
    }
}
