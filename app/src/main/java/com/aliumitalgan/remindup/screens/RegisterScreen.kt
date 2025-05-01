package com.aliumitalgan.remindup.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.aliumitalgan.remindup.ui.theme.BluePrimary
import com.aliumitalgan.remindup.ui.theme.GreenSecondary
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
import com.aliumitalgan.remindup.utils.AuthUtils
import kotlinx.coroutines.launch
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import com.aliumitalgan.remindup.utils.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val TAG = "RegisterScreen"

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    // Hata mesajı gösterimi için LaunchedEffect
    LaunchedEffect(showError) {
        if (showError) {
            // 3 saniye sonra hata mesajını temizle
            kotlinx.coroutines.delay(3000)
            showError = false
        }
    }

    // Google SignIn Client
    val webClientId = context.getString(R.string.web_client_id)
    val googleSignInClient = remember {
        try {
            GoogleAuthHelper.getGoogleSignInClient(context, webClientId)
        } catch (e: Exception) {
            Log.e(TAG, "Google SignIn Client oluştururken hata: ${e.message}", e)
            null
        }
    }

    // Google SignIn Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google SignIn sonucu alındı, kod: ${result.resultCode}")

        isGoogleLoading = true
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            coroutineScope.launch {
                try {
                    Log.d(TAG, "Google hesap bilgisi işleniyor...")
                    val authResult = GoogleAuthHelper.handleSignInResult(task)

                    if (authResult.isSuccess) {
                        Log.d(TAG, "Google ile giriş başarılı")
                        showToast(context, "Google ile giriş başarılı")
                        onRegisterSuccess()
                    } else {
                        val error = authResult.exceptionOrNull()
                        Log.e(TAG, "Google ile giriş başarısız: ${error?.message}", error)
                        errorMessage = error?.message ?: "Google ile giriş başarısız"
                        showError = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Google işleminde beklenmeyen hata: ${e.message}", e)
                    errorMessage = "Beklenmeyen bir hata oluştu: ${e.message}"
                    showError = true
                } finally {
                    isGoogleLoading = false
                }
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google SignIn API hatası: ${e.statusCode} - ${e.message}", e)
            isGoogleLoading = false
            errorMessage = "Google giriş hatası: ${e.message} (Kod: ${e.statusCode})"
            showError = true
        } catch (e: Exception) {
            Log.e(TAG, "Google SignIn genel hatası: ${e.message}", e)
            isGoogleLoading = false
            errorMessage = "Google ile giriş sırasında hata: ${e.message}"
            showError = true
        }
    }

    // Koyu arka plan rengi
    val darkBackgroundColor = Color(0xFF0A1929)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackgroundColor)
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
                modifier = Modifier.padding(top = 32.dp)
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RemindUp",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Yeni Hesap Oluştur",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Hata mesajı
            AnimatedVisibility(
                visible = showError && errorMessage != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Hata",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Kayıt Formu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2D47) // Koyu mavi tonu
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Kayıt Ol",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Name Field - Koyu arka plan üzerinde özel tasarım
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Ad Soyad", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ad Soyad",
                                tint = Color.White
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BluePrimary,
                            unfocusedContainerColor = Color(0xFF102840),
                            focusedContainerColor = Color(0xFF102840)
                        )
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color.White
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
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BluePrimary,
                            unfocusedContainerColor = Color(0xFF102840),
                            focusedContainerColor = Color(0xFF102840)
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Şifre", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şifre",
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster",
                                    tint = Color.White
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BluePrimary,
                            unfocusedContainerColor = Color(0xFF102840),
                            focusedContainerColor = Color(0xFF102840)
                        )
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Şifre Tekrar", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şifre Tekrar",
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster",
                                    tint = Color.White
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BluePrimary,
                            unfocusedContainerColor = Color(0xFF102840),
                            focusedContainerColor = Color(0xFF102840)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Register Button - Mavi buton
                    Button(
                        onClick = {
                            if (validateInput(name, email, password, confirmPassword)) {
                                coroutineScope.launch {
                                    isLoading = true
                                    val result = AuthUtils.registerWithEmail(email, password, name)
                                    isLoading = false

                                    if (result.isSuccess) {
                                        showToast(context, "Kayıt başarılı")
                                        onRegisterSuccess()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Kayıt başarısız"
                                        showError = true
                                    }
                                }
                            } else {
                                errorMessage = "Lütfen tüm alanları doğru şekilde doldurun"
                                showError = true
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Kayıt Ol",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // "ya da" separator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Text(
                    text = "ya da",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
            }

            // Google Sign in Button - Resminize uygun mavi buton
            Button(
                onClick = {
                    isGoogleLoading = true
                    try {
                        Log.d(TAG, "Google SignIn butonu tıklandı")
                        googleSignInClient?.let {
                            Log.d(TAG, "Google SignIn başlatılıyor...")
                            GoogleAuthHelper.signIn(googleSignInLauncher, it)
                        } ?: run {
                            Log.e(TAG, "Google SignIn client null")
                            errorMessage = "Google giriş servisi başlatılamadı"
                            showError = true
                            isGoogleLoading = false
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Google SignIn başlatılırken hata: ${e.message}", e)
                        errorMessage = "Google ile giriş başlatılamadı: ${e.message}"
                        showError = true
                        isGoogleLoading = false
                    }
                },
                enabled = !isGoogleLoading && googleSignInClient != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google icon with white background
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(0.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Google ile Giriş Yap",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Login link
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Zaten hesabınız var mı?",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Giriş Yap",
                        color = BluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Input doğrulama
private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
    return name.isNotEmpty() &&
            email.contains("@") && email.isNotEmpty() &&
            password.length >= 6 &&
            password == confirmPassword
}

// Toast mesajı göster
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}