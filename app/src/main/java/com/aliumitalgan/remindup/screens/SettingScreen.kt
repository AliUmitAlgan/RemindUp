package com.aliumitalgan.remindup.screens

import android.annotation.SuppressLint
import androidx.compose.ui.res.stringResource
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
import com.aliumitalgan.remindup.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.content.Context
import android.util.Log
import com.aliumitalgan.remindup.components.LanguageSelectionDialog
import com.aliumitalgan.remindup.utils.LanguageManager
import com.google.firebase.auth.UserProfileChangeRequest



@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    var isProcessing by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf(currentUser?.displayName ?: "Kullanıcı") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    // Tema durumu
    val isDarkTheme by ThemeManager.isDarkTheme
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by LanguageManager.currentLanguage

    // İsim düzenleme için geçici değişken
    var newUserName by remember { mutableStateOf("") }
    var isUpdatingName by remember { mutableStateOf(false) }

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
                        text = stringResource(R.string.settings_title),
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
        // Dil Seçim Dialog
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                onDismiss = {
                    Log.d("SettingsScreen", "Dialog dismissed")
                    showLanguageDialog = false
                },
                onLanguageSelected = { languageCode ->
                    Log.d("SettingsScreen", "Language selected: $languageCode")
                    try {
                        // Başlangıçtaki dil değerini kaydet
                        val initialLanguage = LanguageManager.currentLanguage.value

                        // Dili değiştir
                        LanguageManager.setLanguage(context, languageCode)

                        // Dil değişiminin gerçekleştiğinden emin olmak için kontrol et
                        val newLanguage = LanguageManager.currentLanguage.value
                        Log.d("SettingsScreen", "Language change result: $initialLanguage -> $newLanguage")

                        // Dil değişimini UI arayüzünde göster - toast mesajı
                        val message = if (languageCode == LanguageManager.LANGUAGE_TURKISH) {
                            context.getString(R.string.language_changed_to_turkish)
                        } else {
                            context.getString(R.string.language_changed_to_english)
                        }
                        showToast(context, message)

                        // Dialog'u kapat
                        showLanguageDialog = false
                    } catch (e: Exception) {
                        Log.e("SettingsScreen", "Dil değiştirme hatası", e)
                        showToast(context, "Dil değiştirme sırasında hata oluştu: ${e.message}")
                    }
                }
            )
        }

        // LazyColumn yerine verticalScroll kullanıyoruz
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()), // Scroll özelliği ekliyoruz
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        onEditProfile = {
                            newUserName = userName // Mevcut ismi başlangıç değeri olarak ata
                            showEditNameDialog = true
                        }
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
                            stringResource(R.string.notifications),
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
                            stringResource(R.string.dark_mode),
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
                            title = stringResource(R.string.language),
                            icon = Icons.Default.Language,
                            onClick = {
                                Log.d("SettingsScreen", "Language section clicked")
                                showLanguageDialog = true
                            },
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        LanguageManager.getLanguageName(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = stringResource(R.string.language),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )

                        SettingsSection(
                            title = stringResource(R.string.settings_premium),
                            icon = Icons.Default.Star,
                            onClick = onNavigateToPremium,
                            trailingContent = {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = stringResource(R.string.settings_premium),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )

                        // Hesap Yönetimi
                        SettingsSection(
                            stringResource(R.string.logout),
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

                        // Kullanıcının daha fazla scroll yapılabildiğini görebilmesi için
                        // ekstra boşluk eklenebilir
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    // İsim Düzenleme Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isUpdatingName) {
                    showEditNameDialog = false
                }
            },
            title = { Text(stringResource(id = R.string.edit_profile)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        label = { Text(stringResource(id = R.string.name)) },
                        singleLine = true,
                        enabled = !isUpdatingName,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isUpdatingName) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Boş isim veya aynı isim kontrolü
                        if (newUserName.isBlank()) {
                            showToast(context, "İsim boş olamaz")
                            return@Button
                        }

                        if (newUserName == userName) {
                            showEditNameDialog = false
                            return@Button
                        }

                        isUpdatingName = true

                        // Firebase'de kullanıcı adını güncelle
                        currentUser?.let { user ->
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(newUserName)
                                .build()

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // İsim başarıyla güncellendi
                                        userName = newUserName
                                        showToast(context, "İsminiz başarıyla güncellendi")
                                    } else {
                                        // Hata durumu
                                        showToast(context, "İsim güncellenirken bir hata oluştu: ${task.exception?.message}")
                                        Log.e("SettingsScreen", "İsim güncelleme hatası", task.exception)
                                    }
                                    isUpdatingName = false
                                    showEditNameDialog = false
                                }
                        } ?: run {
                            // Kullanıcı oturum açmamışsa
                            showToast(context, "Kullanıcı bilgilerine erişilemiyor")
                            isUpdatingName = false
                            showEditNameDialog = false
                        }
                    },
                    enabled = !isUpdatingName && newUserName.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false },
                    enabled = !isUpdatingName
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Çıkış Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(id = R.string.logout)) },
            text = { Text(stringResource(id = R.string.logout_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        // Önce FirebaseAuth'tan çıkış yap
                        AuthUtils.logout()
                        // Dialog'u kapat
                        showLogoutDialog = false
                        // Ardından Login ekranına yönlendir
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.yes_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
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
                    Text(stringResource(R.string.edit_profile))
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
    Log.d("SettingsSection", "Rendering section: $title")
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d("SettingsSection", "Section clicked: $title")
                onClick()
            },
        shape = RoundedCornerShape(16.dp)
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
