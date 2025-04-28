package com.aliumitalgan.remindup.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.utils.AuthUtils
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.aliumitalgan.remindup.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Bildirim durumunu al
    val notificationsEnabled by NotificationUtils.getNotificationStateFlow(context)
        .collectAsState(initial = true)

    // Tema durumunu al
    val isDarkMode by ThemeManager.isDarkTheme

    // Bildirim izni kontrolü
    val hasNotificationPermission = remember { mutableStateOf(NotificationUtils.checkNotificationPermission(context)) }

    // Bildirim izni launcher'ı
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission.value = isGranted
        if (isGranted) {
            // İzin verildiğinde bildirimleri aç
            coroutineScope.launch {
                NotificationUtils.saveNotificationState(context, true)
            }
        } else {
            // İzin reddedildiğinde bildirimleri kapat
            coroutineScope.launch {
                NotificationUtils.saveNotificationState(context, false)
            }
            showPermissionDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil ve Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Kullanıcı profil bölümü
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profil fotoğrafı (baş harfler)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.displayName?.firstOrNull()?.toString()
                                ?: currentUser?.email?.firstOrNull()?.toString()
                                ?: "U",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kullanıcı adı
                    Text(
                        text = currentUser?.displayName ?: "Kullanıcı",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // E-posta
                    Text(
                        text = currentUser?.email ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profil düzenle butonu
                    OutlinedButton(
                        onClick = { /* Profil düzenleme işlemleri */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profili Düzenle")
                    }
                }
            }

            // Ayarlar Bölümü
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Ayarlar",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Bildirimler Ayarı
                    ListItem(
                        headlineContent = { Text("Bildirimler") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Bildirimler"
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { isEnabled ->
                                    if (isEnabled && !hasNotificationPermission.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        // İzin istenmesi gerekiyor
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        // İzin zaten var veya gerekmiyor, durumu kaydet
                                        coroutineScope.launch {
                                            NotificationUtils.saveNotificationState(context, isEnabled)
                                        }

                                        Toast.makeText(
                                            context,
                                            if (isEnabled) "Bildirimler açıldı" else "Bildirimler kapatıldı",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    )

                    Divider()

                    // Karanlık Mod Ayarı
                    ListItem(
                        headlineContent = { Text("Karanlık Mod") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Karanlık Mod"
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { isEnabled ->
                                    coroutineScope.launch {
                                        ThemeManager.saveDarkThemeState(context, isEnabled)
                                    }
                                    Toast.makeText(
                                        context,
                                        if (isEnabled) "Karanlık mod açıldı" else "Karanlık mod kapatıldı",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    )

                    Divider()

                    // Dil Ayarı
                    ListItem(
                        headlineContent = { Text("Dil") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Dil"
                            )
                        },
                        trailingContent = {
                            Text("Türkçe")
                        },
                        modifier = Modifier.clickable {
                            // Dil seçim işlemleri
                            Toast.makeText(
                                context,
                                "Bu özellik yakında eklenecek",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Hakkında ve Destek Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Hakkında ve Destek",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Destek Merkezi
                    ListItem(
                        headlineContent = { Text("Destek Merkezi") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "Destek"
                            )
                        },
                        modifier = Modifier.clickable {
                            // Destek merkezine yönlendirme
                            Toast.makeText(
                                context,
                                "Destek merkezine yönlendiriliyorsunuz",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    Divider()

                    // Uygulama Hakkında
                    ListItem(
                        headlineContent = { Text("Uygulama Hakkında") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Hakkında"
                            )
                        },
                        modifier = Modifier.clickable {
                            // Uygulama hakkında bilgi ekranına yönlendirme
                            Toast.makeText(
                                context,
                                "RemindUp v1.0 - 2025",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    Divider()

                    // Gizlilik Politikası
                    ListItem(
                        headlineContent = { Text("Gizlilik Politikası") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Gizlilik"
                            )
                        },
                        modifier = Modifier.clickable {
                            // Gizlilik politikasına yönlendirme
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://www.remindup.com/privacy")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Gizlilik politikası sayfası bulunamadı",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            // Çıkış Butonu
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Çıkış Yap"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çıkış Yap")
            }
        }

        // Çıkış Yap Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Çıkış Yap") },
                text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                AuthUtils.logout()
                                showLogoutDialog = false
                                // Login ekranına yönlendirme
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("Evet, Çıkış Yap")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text("İptal")
                    }
                }
            )
        }

        // Bildirim İzni Dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Bildirim İzni") },
                text = {
                    Text("Bildirimlerin düzgün çalışması için izin vermeniz gerekiyor. Uygulama ayarlarına gidip bildirimlere izin vermek ister misiniz?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Uygulama ayarlarına yönlendir
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            showPermissionDialog = false
                        }
                    ) {
                        Text("Ayarlara Git")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPermissionDialog = false }
                    ) {
                        Text("İptal")
                    }
                }
            )
        }
    }
}