package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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

private val LightBg = Color(0xFFFDFBF9)
private val AccentOrange = Color(0xFFF26522)
private val Deep = Color(0xFF1A1A1A)
private val SecureGreen = Color(0xFF22C55E)

@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    onLogoutAll: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {}
) {
    var twoFactorEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("security") }
    val navItems = mainBottomNavItems()

    Scaffold(
        containerColor = LightBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Deep)
                }
                Text("Security Settings", fontWeight = FontWeight.Bold, color = Deep, fontSize = 18.sp)
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
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
                        "analytic" -> { }
                        "settings" -> onNavigateToSettings()
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(40.dp))
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SecureGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your account is secure", fontWeight = FontWeight.Bold, color = Deep, fontSize = 20.sp)
                    Text("We recommend reviewing your security settings every 90 days.", fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }

            item {
                Text("LOGIN & ACCESS", fontSize = 12.sp, color = AccentOrange, fontWeight = FontWeight.SemiBold)
            }

            item {
                SecurityRow(
                    icon = Icons.Filled.Refresh,
                    title = "Change Password",
                    subtitle = "Last updated 3 months ago",
                    showArrow = true,
                    onClick = { }
                )
            }
            item {
                SecurityRow(
                    icon = Icons.Filled.Devices,
                    title = "2-Factor Authentication",
                    subtitle = "Secure via SMS or App",
                    showArrow = false,
                    toggleChecked = twoFactorEnabled,
                    onToggleChange = { twoFactorEnabled = it }
                )
            }
            item {
                SecurityRow(
                    icon = Icons.Filled.Face,
                    title = "FaceID / Biometric",
                    subtitle = "Unlock with your face",
                    showArrow = false,
                    toggleChecked = biometricEnabled,
                    onToggleChange = { biometricEnabled = it }
                )
            }

            item {
                Text("ACCOUNT PRIVACY", fontSize = 12.sp, color = AccentOrange, fontWeight = FontWeight.SemiBold)
            }

            item {
                SecurityRow(
                    icon = Icons.Filled.Devices,
                    title = "Active Sessions",
                    subtitle = "2 devices currently logged in",
                    showArrow = true,
                    onClick = { }
                )
            }
            item {
                SecurityRow(
                    icon = Icons.Filled.Description,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    showArrow = false,
                    showExternalLink = true,
                    onClick = { }
                )
            }

            item {
                Surface(
                    onClick = onLogoutAll,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null, tint = Color(0xFFE53935))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Log out from all devices", color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SecurityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    showArrow: Boolean = false,
    showExternalLink: Boolean = false,
    toggleChecked: Boolean = false,
    onToggleChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA),
        onClick = if (showArrow || showExternalLink) onClick else ({})
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Deep, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            when {
                showArrow -> Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF9CA3AF))
                showExternalLink -> Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = Color(0xFF9CA3AF))
                else -> Switch(
                    checked = toggleChecked,
                    onCheckedChange = onToggleChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentOrange
                    )
                )
            }
        }
    }
}
