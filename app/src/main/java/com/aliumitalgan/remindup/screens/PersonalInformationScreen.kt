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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

private val LightBg = Color(0xFFFDFBF9)
private val AccentOrange = Color(0xFFF26522)
private val Deep = Color(0xFF1A1A1A)
private val LightPeach = Color(0xFFFFF3E8)
private val LightPurple = Color(0xFFF3E8FF)
private val LightMint = Color(0xFFE8F5E9)

@Composable
fun PersonalInformationScreen(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    var fullName by remember { mutableStateOf(user?.displayName ?: "Jane Cooper") }
    var email by remember { mutableStateOf(user?.email ?: "jane.cooper@example.com") }
    var phone by remember { mutableStateOf("+1 (555) 000-1234") }
    var bio by remember { mutableStateOf("Product designer and productivity enthusiast. Love keeping my schedule organized!") }

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = AccentOrange)
                }
                Text("Settings", fontWeight = FontWeight.Bold, color = Deep, fontSize = 18.sp)
                TextButton(onClick = onSave) {
                    Text("Save", color = AccentOrange, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightPeach),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset((-4).dp, (-4).dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AccentOrange
                )
            ) {
                Text("Change Photo", fontWeight = FontWeight.SemiBold)
            }

            Text(
                "Personal Information",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Deep,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                "Update your profile details below",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                "ACCOUNT DETAILS",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 24.dp)
            )

            ProfileTextField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                icon = Icons.Filled.Person,
                iconBg = LightPeach
            )
            ProfileTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                icon = Icons.Filled.Email,
                iconBg = LightPurple
            )
            ProfileTextField(
                label = "Phone Number",
                value = phone,
                onValueChange = { phone = it },
                icon = Icons.Filled.Phone,
                iconBg = LightMint
            )
            ProfileTextField(
                label = "Short Bio",
                value = bio,
                onValueChange = { bio = it },
                icon = Icons.Filled.Description,
                iconBg = LightPeach,
                minLines = 3
            )

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White)
            ) {
                Text("Update Profile", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280))
            ) {
                Text("Deactivate Account", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    iconBg: Color,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Deep) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF4A4A4A))
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentOrange,
            focusedLabelColor = AccentOrange,
            cursorColor = AccentOrange,
            focusedLeadingIconColor = AccentOrange
        )
    )
}
