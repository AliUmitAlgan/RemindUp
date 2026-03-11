package com.aliumitalgan.remindup.screens

import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.presentation.security.SecurityViewModel
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.BiometricAuthManager
import kotlinx.coroutines.launch

private val ScreenBg: Color
    get() = themedColor(Color(0xFFFCF8F6), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val Deep: Color
    get() = themedColor(Color(0xFF1C2635), Color(0xFFE5E7EB))

@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    onLogoutAll: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    viewModel: SecurityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findFragmentActivity() }
    val biometricAuthManager = remember(context) { BiometricAuthManager(context) }
    val scope = rememberCoroutineScope()
    val skipFirstResumeRefresh = remember { mutableStateOf(true) }

    fun refreshBiometricCapability() {
        val capability = biometricAuthManager.getCapability()
        viewModel.updateBiometricCapability(
            capability = capability,
            statusText = biometricAuthManager.capabilityMessage(capability)
        )
    }

    LaunchedEffect(Unit) {
        refreshBiometricCapability()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshBiometricCapability()
                if (skipFirstResumeRefresh.value) {
                    skipFirstResumeRefresh.value = false
                } else {
                    viewModel.refreshSecurityPreferences(showLoading = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun onBiometricToggle(nextValue: Boolean) {
        if (uiState.isUpdatingBiometric) return

        if (!nextValue) {
            viewModel.updateBiometricEnabled(false)
            return
        }

        if (!uiState.isBiometricAvailable) {
            viewModel.showError(uiState.biometricStatusText)
            return
        }

        val currentActivity = activity
        if (currentActivity == null) {
            viewModel.showError("Biometric prompt could not be started on this screen.")
            return
        }

        scope.launch {
            val result = biometricAuthManager.authenticate(
                activity = currentActivity,
                title = "Enable Biometric Unlock",
                subtitle = "Confirm your identity",
                description = "Use your device biometric credential to enable biometric unlock."
            )
            if (result.success) {
                viewModel.updateBiometricEnabled(true)
            } else if (!result.cancelled) {
                viewModel.showError(result.errorMessage ?: "Biometric authentication failed.")
            }
        }
    }

    val biometricSubtitle = when {
        uiState.biometricEnabled && uiState.isBiometricAvailable ->
            "Biometric unlock is active for this device."
        uiState.isBiometricAvailable ->
            "Use fingerprint or face recognition to unlock the app."
        else -> uiState.biometricStatusText
    }

    Scaffold(containerColor = ScreenBg) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEFE5))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AccentOrange
                            )
                        }
                        Text(
                            text = "Security Settings",
                            modifier = Modifier.align(Alignment.Center),
                            fontWeight = FontWeight.Bold,
                            color = Deep,
                            fontSize = 20.sp
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AccentOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3CC46B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your account is secure",
                            color = Deep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Text(
                            text = "We recommend reviewing your security settings every 90 days.",
                            color = Color(0xFF9AA3B0),
                            fontSize = 13.sp
                        )
                    }
                }

                uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { error ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF1F1),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color(0xFFFFC6C6)
                            )
                        ) {
                            Text(
                                text = error,
                                color = Color(0xFFB3261E),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                item {
                    SectionLabel("LOGIN & ACCESS")
                }

                item {
                    SecuritySettingRow(
                        icon = Icons.Filled.LockReset,
                        title = "Change Password",
                        subtitle = "Last updated 3 months ago",
                        onClick = onNavigateToChangePassword,
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFFB6BCC8)
                            )
                        }
                    )
                }

                item {
                    SecuritySettingRow(
                        icon = Icons.Filled.Fingerprint,
                        title = "Biometric Unlock",
                        subtitle = biometricSubtitle,
                        trailing = {
                            if (uiState.isUpdatingBiometric) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = AccentOrange
                                )
                            } else {
                                Switch(
                                    checked = uiState.biometricEnabled,
                                    onCheckedChange = { onBiometricToggle(it) },
                                    enabled = uiState.isBiometricAvailable || uiState.biometricEnabled,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AccentOrange,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFE2E7EF)
                                    )
                                )
                            }
                        }
                    )
                }

                item {
                    SectionLabel("ACCOUNT PRIVACY")
                }

                item {
                    SecuritySettingRow(
                        icon = Icons.Filled.Devices,
                        title = "Active Sessions",
                        subtitle = "2 devices currently logged in",
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFFB6BCC8)
                            )
                        }
                    )
                }

                item {
                    SecuritySettingRow(
                        icon = Icons.Filled.Description,
                        title = "Privacy Policy",
                        subtitle = "How we handle your data",
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFFB6BCC8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                item {
                    Surface(
                        onClick = onLogoutAll,
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFFFC9AF), RoundedCornerShape(16.dp))
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = Color(0xFFF15A24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Log out from all devices",
                                color = Color(0xFFF15A24),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = AccentOrange,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp
    )
}

@Composable
private fun SecuritySettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = themedColor(Color.White, Color(0xFF1A2230))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themedColor(Color(0xFFFFF0E8), Color(0xFF2A3548))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Deep,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = subtitle,
                    color = themedColor(Color(0xFF9AA3B0), Color(0xFF9AA6B2)),
                    fontSize = 12.sp
                )
            }
            trailing()
        }
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
