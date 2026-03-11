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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.presentation.security.ChangePasswordViewModel
import com.aliumitalgan.remindup.ui.theme.themedColor
import kotlinx.coroutines.delay

private val ScreenBackground: Color
    get() = themedColor(Color(0xFFF5F6F8), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF08A57)
private val MainText: Color
    get() = themedColor(Color(0xFF0F172A), Color(0xFFE5E7EB))
private val SecondaryText: Color
    get() = themedColor(Color(0xFF64748B), Color(0xFF94A3B8))
private val FieldBorder: Color
    get() = themedColor(Color(0xFF7C879B), Color(0xFF475569))
private val FieldBackground: Color
    get() = themedColor(Color.White, Color(0xFF1A2230))

@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    onPasswordUpdated: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(300)
            viewModel.consumeSuccess()
            onPasswordUpdated()
        }
    }

    Scaffold(containerColor = ScreenBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(themedColor(Color.White, Color(0xFF1A2230)))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MainText
                        )
                    }

                    Text(
                        text = "Change Password",
                        color = MainText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(38.dp))
                            .background(Color(0xFFEFC0AA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PasswordField(
                        label = "Current Password",
                        value = uiState.currentPassword,
                        onValueChange = viewModel::updateCurrentPassword,
                        visible = uiState.showCurrentPassword,
                        onToggleVisibility = viewModel::toggleCurrentPasswordVisibility
                    )

                    PasswordField(
                        label = "New Password",
                        value = uiState.newPassword,
                        onValueChange = viewModel::updateNewPassword,
                        visible = uiState.showNewPassword,
                        onToggleVisibility = viewModel::toggleNewPasswordVisibility,
                        placeholder = "At least 8 characters"
                    )

                    RequirementRow(
                        text = "At least 8 characters",
                        fulfilled = uiState.hasMinLength
                    )
                    RequirementRow(
                        text = "One special character (@, #, $, etc.)",
                        fulfilled = uiState.hasSpecialCharacter
                    )

                    PasswordField(
                        label = "Confirm New Password",
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::updateConfirmPassword,
                        visible = uiState.showConfirmPassword,
                        onToggleVisibility = viewModel::toggleConfirmPasswordVisibility
                    )

                    uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFE11D48),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    uiState.successMessage?.takeIf { it.isNotBlank() }?.let { success ->
                        Text(
                            text = success,
                            color = Color(0xFF16A34A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Button(
                onClick = viewModel::submitPasswordChange,
                enabled = uiState.canSubmit,
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange,
                    contentColor = Color.White,
                    disabledContainerColor = AccentOrange.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .height(64.dp)
            ) {
                Text(
                    text = if (uiState.isUpdating) "Updating..." else "Update Password",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    placeholder: String = ""
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = SecondaryText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(
                        text = placeholder,
                        color = SecondaryText,
                        fontSize = 16.sp
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = SecondaryText
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                disabledContainerColor = FieldBackground,
                focusedBorderColor = FieldBorder,
                unfocusedBorderColor = FieldBorder,
                focusedTextColor = MainText,
                unfocusedTextColor = MainText,
                focusedLabelColor = SecondaryText,
                unfocusedLabelColor = SecondaryText,
                cursorColor = AccentOrange
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RequirementRow(
    text: String,
    fulfilled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (fulfilled) Color(0xFFD1FAE5) else themedColor(Color(0xFFE2E8F0), Color(0xFF334155)))
                .border(
                    width = 1.dp,
                    color = if (fulfilled) Color(0xFFBBF7D0) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (fulfilled) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(themedColor(Color(0xFF94A3B8), Color(0xFF64748B)))
                )
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = text,
            color = SecondaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
