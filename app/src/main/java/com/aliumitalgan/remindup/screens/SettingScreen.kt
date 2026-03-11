package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.MoreActionsBottomSheet
import com.aliumitalgan.remindup.components.SignOutConfirmationDialog
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.data.repository.FirestoreEntitlementRepository
import com.aliumitalgan.remindup.domain.model.EntitlementStatus
import com.aliumitalgan.remindup.domain.model.PlanType
import com.aliumitalgan.remindup.utils.AuthUtils
import com.aliumitalgan.remindup.utils.ProgressUtils
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.google.firebase.auth.FirebaseAuth

private val ProfileBg: Color
    get() = themedColor(Color(0xFFF5F2F2), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val Deep: Color
    get() = themedColor(Color(0xFF161B38), Color(0xFFE5E7EB))

@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {},
    onNavigateToPersonalInfo: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {}
) {
    var currentRoute by remember { mutableStateOf("settings") }
    val navItems = mainBottomNavItems()

    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName?.ifBlank { null } ?: "Sweet Reminder"
    val email = user?.email ?: "remindup.user@sweet.com"

    var tasksDone by remember { mutableStateOf(128) }
    var streaks by remember { mutableStateOf(42) }
    var awards by remember { mutableStateOf(12) }
    var isPremium by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showMoreActions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching {
            val entitlement = FirestoreEntitlementRepository().getEntitlement()
            isPremium = entitlement.planType == PlanType.PREMIUM &&
                (entitlement.status == EntitlementStatus.ACTIVE || entitlement.status == EntitlementStatus.GRACE)
        }

        runCatching {
            val goals = ProgressUtils.getUserGoals().getOrDefault(emptyList()).map { it.second }
            if (goals.isNotEmpty()) {
                tasksDone = (goals.sumOf { it.progress } / 3).coerceAtLeast(8)
                streaks = goals.count { it.progress >= 60 }.coerceAtLeast(1) * 3
                awards = goals.count { it.progress >= 100 }.coerceAtLeast(1) * 2
            }
        }
    }

    Scaffold(
        containerColor = ProfileBg,
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> onNavigateToGoals()
                        "social" -> onNavigateToSocial()
                        "analytic" -> onNavigateToProgress()
                        "settings" -> Unit
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Deep)
                    }
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.ExtraBold,
                        color = Deep,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showMoreActions = true }) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = null, tint = Deep)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(106.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD7C4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AccentOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = Deep)
                    Text(email, color = AccentOrange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = themedColor(Color(0xFFF1F2F5), Color(0xFF232D3A)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Joined May 2023",
                            color = themedColor(Color(0xFF8B92A9), Color(0xFFAEB6C5)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        value = tasksDone.toString(),
                        label = "TASKS\nDONE",
                        valueColor = Color(0xFFEC5A9B),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        value = streaks.toString(),
                        label = "STREAKS",
                        valueColor = Color(0xFF43A2FF),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        value = awards.toString(),
                        label = "AWARDS",
                        valueColor = Color(0xFF43D3B8),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Surface(
                    color = themedColor(Color(0xFFFFF7E8), Color(0xFF202A37)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToPremium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCCB43)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPremium) "Premium Member" else "Free Member",
                                fontWeight = FontWeight.ExtraBold,
                                color = Deep
                            )
                            Text(
                                text = "Unlocking sweet possibilities",
                                color = AccentOrange,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF63CF88))
                        )
                    }
                }
            }

            item {
                Text("Account Settings", fontWeight = FontWeight.ExtraBold, color = Deep, fontSize = 21.sp)
            }

            item {
                SettingRow(
                    icon = Icons.Filled.PersonOutline,
                    title = "Personal Information",
                    subtitle = "Update your bio and photo",
                    onClick = onNavigateToPersonalInfo
                )
            }
            item {
                SettingRow(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    subtitle = "Manage your sweet alerts",
                    onClick = onNavigateToNotifications
                )
            }
            item {
                SettingRow(
                    icon = Icons.Filled.Security,
                    title = "Security",
                    subtitle = "Password and privacy settings",
                    onClick = onNavigateToSecurity
                )
            }
            item {
                SettingRow(
                    icon = Icons.Filled.Palette,
                    title = "Appearance",
                    subtitle = "Theme and colors",
                    onClick = onNavigateToAppearance
                )
            }

            item {
                Surface(
                    color = themedColor(Color(0xFFF8EDEE), Color(0xFF2A2023)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Sign Out", color = Color(0xFFFF3B30), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    if (showSignOutDialog) {
        SignOutConfirmationDialog(
            onDismiss = { showSignOutDialog = false },
            onConfirmSignOut = {
                AuthUtils.logout()
                showSignOutDialog = false
                onLogout()
            }
        )
    }

    if (showMoreActions) {
        MoreActionsBottomSheet(
            onDismiss = { showMoreActions = false },
            onShareProfile = { showMoreActions = false },
            onExportData = { showMoreActions = false },
            onHelpCenter = { showMoreActions = false },
            onReportProblem = { showMoreActions = false }
        )
    }
}

@Composable
private fun MetricCard(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = themedColor(Color(0xFFF9FAFC), Color(0xFF1D2632)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = valueColor, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
            Text(
                text = label,
                color = themedColor(Color(0xFFABB2C5), Color(0xFFAEB6C5)),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = themedColor(Color(0xFFFAFAFA), Color(0xFF171D26)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFCECDD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Deep,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF8A90A8),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFB7BDD2))
        }
    }
}
