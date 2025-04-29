package com.aliumitalgan.remindup.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.utils.AuthUtils
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.aliumitalgan.remindup.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onLogout:       () -> Unit

) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    var isProcessing by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf(currentUser?.displayName ?: "Kullanıcı") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Tema durumu
    val isDarkTheme by ThemeManager.isDarkTheme
    val coroutineScope = rememberCoroutineScope()
    // Bildirim durumu
    var notificationsEnabled by remember {
        mutableStateOf(NotificationUtils.loadNotificationState(context))
    }

    // Bottom Navigation Items
    val bottomNavItems = listOf(
        BottomNavItem("Ana Sayfa", Icons.Filled.Home, Icons.Filled.Home, "home"),
        BottomNavItem("Hedefler", Icons.Filled.CheckCircle, Icons.Filled.CheckCircle, "goals"),
        BottomNavItem("Hatırlatıcılar", Icons.Filled.Notifications, Icons.Filled.Notifications, "reminders"),
        BottomNavItem("İlerleme", Icons.Filled.ShowChart, Icons.Filled.ShowChart, "progress"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Filled.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[4].route) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ayarlar",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                        "progress" -> onNavigateToProgress()
                        "profile" -> {} // Zaten profil ekranındayız
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Kartı
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInHorizontally(
                            animationSpec = tween(1000),
                            initialOffsetX = { -it }
                        )
            ) {
                ProfileSection(
                    userName = userName,
                    userEmail = currentUser?.email ?: "",
                    onEditProfile = { showEditNameDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ayarlar Bölümleri
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 300))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bildirim Ayarları
                    SettingsSection(
                        title = "Bildirimler",
                        icon = Icons.Default.Notifications,
                        trailingContent = {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { newState ->
                                        // İşlem devam ederken kullanıcının başka bir işlem yapmasını engelle
                                        isProcessing = true

                                        coroutineScope.launch {
                                            try {
                                                // NotificationUtils'in saveNotificationState metodunu çağır
                                                NotificationUtils.saveNotificationState(context, newState)

                                                // UI thread'inde state'i güncelle
                                                withContext(Dispatchers.Main) {
                                                    notificationsEnabled = newState
                                                    showToast(
                                                        context,
                                                        if (newState) "Bildirimler açıldı" else "Bildirimler kapatıldı"
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                // Hata durumunda kullanıcıya bilgi ver
                                                showToast(context, "İşlem sırasında hata oluştu: ${e.message}")
                                                Log.e("SettingsScreen", "Bildirim durumu güncelleme hatası", e)
                                            } finally {
                                                // İşlem bittiğinde işlemi sonlandır
                                                isProcessing = false
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    // İşlem devam ederken switch'i devre dışı bırak
                                    enabled = !isProcessing
                                )
                            }
                        }
                    )

                    // Tema Ayarları
                    SettingsSection(
                        title = "Karanlık Mod",
                        icon = Icons.Default.DarkMode,
                        trailingContent = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = {
                                    ThemeManager.saveDarkThemeState(context, it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    )

                    // Dil Ayarları
                    SettingsSection(
                        title = "Dil",
                        icon = Icons.Default.Language,
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Türkçe",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Dil Seç",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    // Hesap Yönetimi
                    SettingsSection(
                        title = "Çıkış Yap",
                        icon = Icons.Default.Logout,
                        onClick = { showLogoutDialog = true },
                        trailingContent = {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Çıkış",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }

    // İsim Düzenleme Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap") },
            text  = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        AuthUtils.logout()   // önce oturumu kapat
                        onLogout()           // sonra dışarıya bildir
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Çıkış Yap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Çıkış Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap") },
            text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        AuthUtils.logout()
                        showLogoutDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Evet, Çıkış Yap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun ProfileSection(
    userName: String,
    userEmail: String,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profil Fotoğrafı
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onEditProfile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Profili Düzenle"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profili Düzenle")
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingContent: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            trailingContent()
        }
    }
}
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
