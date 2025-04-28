package com.aliumitalgan.remindup.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.ui.theme.BluePrimary
import com.aliumitalgan.remindup.ui.theme.GreenSecondary
import com.aliumitalgan.remindup.utils.AnimationUtils
import com.aliumitalgan.remindup.utils.AuthUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Google SignIn Client
    val googleSignInClient = remember {
        AuthUtils.getGoogleSignInClient(
            context,
            context.getString(R.string.web_client_id) // R.string.web_client_id değerini strings.xml'e ekleyin
        )
    }

    // Google SignIn Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            coroutineScope.launch {
                isLoading = true
                val authResult = AuthUtils.handleGoogleSignInResult(task)
                isLoading = false

                if (authResult.isSuccess) {
                    showToast(context, "Google ile giriş başarılı")
                    onLoginSuccess()
                } else {
                    showToast(context, "Google ile giriş başarısız: ${authResult.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
                }
            }
        } catch (e: ApiException) {
            isLoading = false
            showToast(context, "Google ile giriş hatası: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo ve Başlık
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    BluePrimary,
                                    GreenSecondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // İkon içindeki tik ve ok simgesini çizim olarak ekleyin
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "RemindUp Logo",
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RemindUp",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hedeflerinizi Takip Edin, Hatırlatıcılarınızı Yönetin",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Giriş Formu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Giriş Yap",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Şifre") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login Button
                    Button(
                        onClick = {
                            if (validateInput(email, password)) {
                                coroutineScope.launch {
                                    isLoading = true
                                    val result = AuthUtils.loginWithEmail(email, password)
                                    isLoading = false

                                    if (result.isSuccess) {
                                        showToast(context, "Giriş başarılı")
                                        onLoginSuccess()
                                    } else {
                                        showToast(context, "Giriş başarısız: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
                                    }
                                }
                            } else {
                                showToast(context, "Lütfen geçerli bir email ve şifre girin")
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Giriş Yap", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Or separator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                )
                Text(
                    text = "ya da",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                )
            }

            // Google Sign in Button
            Button(
                onClick = {
                    AuthUtils.signInWithGoogle(googleSignInClient, googleSignInLauncher)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google ile Giriş Yap", fontSize = 16.sp)
                }
            }

            // Register link
            Row(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Hesabınız yok mu?",
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        "Kayıt Olun",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Input doğrulama
private fun validateInput(email: String, password: String): Boolean {
    return email.contains("@") && email.isNotEmpty() && password.length >= 6
}

// Toast mesajı göster
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}