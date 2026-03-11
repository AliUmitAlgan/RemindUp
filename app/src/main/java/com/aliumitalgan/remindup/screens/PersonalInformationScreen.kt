package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.appCardColor
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.google.firebase.auth.FirebaseAuth

private val ScreenBg: Color
    get() = themedColor(Color(0xFFF6F2F2), Color(0xFF0F131A))
private val Primary = Color(0xFFEC5B13)
private val Deep: Color
    get() = themedColor(Color(0xFF1F2937), Color(0xFFE5E7EB))
private val Peach = Color(0xFFFFF1E6)
private val Mint = Color(0xFFE6F7F1)
private val Lavender = Color(0xFFF3F0FF)

@Composable
fun PersonalInformationScreen(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    var fullName by remember { mutableStateOf(user?.displayName ?: "Jane Cooper") }
    var email by remember { mutableStateOf(user?.email ?: "jane.cooper@example.com") }
    var phone by remember { mutableStateOf("+1 (555) 000-1234") }
    var bio by remember {
        mutableStateOf("Product designer and productivity enthusiast. Love keeping my schedule organized!")
    }

    Scaffold(containerColor = ScreenBg) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = themedColor(Color.White.copy(alpha = 0.86f), Color(0xFF171D26)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Primary.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Peach)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Primary
                        )
                    }

                    Text(
                        text = "Settings",
                        color = Deep,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    TextButton(
                        onClick = onSave,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            text = "Save",
                            color = Primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Peach,
                            modifier = Modifier.size(96.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                3.dp,
                                themedColor(Color.White, Color(0xFF2B3545))
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF9F7A60),
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Primary)
                                .border(2.dp, themedColor(Color.White, Color(0xFF2B3545)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Personal Information",
                        color = Deep,
                        fontSize = 33.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Update your profile details below",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Change Photo",
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ACCOUNT DETAILS",
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                AccountDetailField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    icon = Icons.Filled.Person,
                    iconTint = Primary,
                    iconBackground = Peach
                )

                AccountDetailField(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Filled.Mail,
                    iconTint = Color(0xFF6366F1),
                    iconBackground = Lavender
                )

                AccountDetailField(
                    label = "Phone Number",
                    value = phone,
                    onValueChange = { phone = it },
                    icon = Icons.Filled.Call,
                    iconTint = Color(0xFF10B981),
                    iconBackground = Mint
                )

                AccountDetailField(
                    label = "Short Bio",
                    value = bio,
                    onValueChange = { bio = it },
                    icon = Icons.Filled.Description,
                    iconTint = Primary,
                    iconBackground = Peach,
                    minLines = 4
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Update Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Deactivate Account",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun AccountDetailField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    minLines: Int = 1
) {
    val fieldHeight = if (minLines > 1) 124.dp else 56.dp

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = themedColor(Color(0xFF334155), Color(0xFFE5E7EB)),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Primary.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                .background(appCardColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .fillMaxHeight()
                    .background(iconBackground),
                contentAlignment = if (minLines > 1) Alignment.TopCenter else Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .padding(top = if (minLines > 1) 14.dp else 0.dp)
                        .size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Primary.copy(alpha = 0.15f))
            )

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                minLines = minLines,
                maxLines = if (minLines > 1) 6 else 1,
                singleLine = minLines == 1,
                textStyle = TextStyle(
                    color = Deep,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = appCardColor,
                    unfocusedContainerColor = appCardColor,
                    disabledContainerColor = appCardColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Primary
                )
            )
        }
    }
}
