package com.aliumitalgan.remindup.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.themedColor

private val AccentOrange = Color(0xFFF26522)
private val LightBg: Color
    get() = themedColor(Color(0xFFFBF8F4), Color(0xFF0F131A))

@Composable
fun PasswordResetConfirmationScreen(
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AccentOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = AccentOrange
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Check your inbox!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "We just sent a magic link to your email. It might take a minute to arrive!",
                fontSize = 16.sp,
                color = themedColor(Color(0xFF6B7280), Color(0xFF9CA3AF)),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themedColor(Color(0xFFE5E7EB), Color(0xFF1F2937)),
                    contentColor = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB))
                )
            ) {
                Text("Back to Login", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
