package com.aliumitalgan.remindup.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.google.firebase.auth.FirebaseAuth

private val AccentOrange = Color(0xFFF26522)
private val LightBg: Color
    get() = themedColor(Color(0xFFFBF8F4), Color(0xFF0F131A))

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetEmailSent: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AccentOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LockReset,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AccentOrange
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Forgot Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No worries! Enter your email and we'll send you a link to get back in.",
                fontSize = 16.sp,
                color = themedColor(Color(0xFF6B7280), Color(0xFFAEB6C5)),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("hello@remindup.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentOrange,
                    unfocusedBorderColor = themedColor(Color(0xFFE5E7EB), Color(0xFF334155)),
                    focusedLabelColor = AccentOrange,
                    unfocusedLabelColor = themedColor(Color(0xFF6B7280), Color(0xFFAEB6C5)),
                    focusedTextColor = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB)),
                    unfocusedTextColor = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB)),
                    focusedPlaceholderColor = themedColor(Color(0xFF9CA3AF), Color(0xFF94A3B8)),
                    unfocusedPlaceholderColor = themedColor(Color(0xFF9CA3AF), Color(0xFF94A3B8))
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (email.isNotBlank() && email.contains("@")) {
                        isLoading = true
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    onResetEmailSent()
                                } else {
                                    Toast.makeText(
                                        context,
                                        task.exception?.message ?: "Failed to send reset email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) {
                Text("Send Reset Link", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = themedColor(Color(0xFF6B7280), Color(0xFFAEB6C5))
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Back to Login", color = themedColor(Color(0xFF6B7280), Color(0xFFAEB6C5)))
            }
        }
    }
}
