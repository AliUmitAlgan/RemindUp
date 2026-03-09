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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.components.MoreActionsBottomSheet
import com.aliumitalgan.remindup.components.SignOutConfirmationDialog
import com.aliumitalgan.remindup.data.repository.FirestoreEntitlementRepository
import com.aliumitalgan.remindup.domain.model.EntitlementStatus
import com.aliumitalgan.remindup.domain.model.PlanType
import com.aliumitalgan.remindup.utils.AuthUtils
import com.google.firebase.auth.FirebaseAuth

private val ProfileBg = Color(0xFFF5F2F2)
private val Orange = Color(0xFFF26522)
private val Deep = Color(0xFF161B38)

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
    onNavigateToSecurity: () -> Unit = {}
) {
    var currentRoute by remember { mutableStateOf("settings") }
    val navItems = mainBottomNavItems()

    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName?.ifBlank { null } ?: "Sweet Reminder"
    val email = user?.email ?: "remindup.user@sweet.com"
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifBlank { "SR" }

    var isPremium by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showMoreActions by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        runCatching {
            val entitlement = FirestoreEntitlementRepository().getEntitlement()
            isPremium = entitlement.planType == PlanType.PREMIUM &&
                (entitlement.status == EntitlementStatus.ACTIVE || entitlement.status == EntitlementStatus.GRACE)
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
                },
                onCenterActionClick = onNavigateToGoals
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Deep)
                    }
                    Text("RemindUp Sweet Profile", fontWeight = FontWeight.ExtraBold, color = Deep)
                    Row {
                        IconButton(onClick = { showMoreActions = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Deep)
                        }
                        IconButton(onClick = onNavigateToPremium) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Orange)
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD7C4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = Color.White, fontWeight = FontWeight.Black, fontSize = 30.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = Deep)
                    Text(email, color = Orange, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Surface(
                    color = Color(0xFFFFF5DF),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToPremium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Orange, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isPremium) "Premium Member" else "Free Member",
                                fontWeight = FontWeight.ExtraBold,
                                color = Deep
                            )
                            Text(
                                if (isPremium) "Unlocking sweet possibilities" else "Upgrade to unlock sweet possibilities",
                                color = Orange,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isPremium) Color(0xFF22C55E) else Color(0xFF9CA3AF))
                        )
                    }
                }
            }

            item { Text("Account Settings", fontWeight = FontWeight.ExtraBold, color = Deep, fontSize = 20.sp) }

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
                    subtitle = "Theme and colors"
                )
            }

            item {
                Surface(
                    color = Color(0xFFF8EDEE),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
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
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = Color(0xFFFAFAFA),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick ?: {}
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
                Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(18.dp))
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
