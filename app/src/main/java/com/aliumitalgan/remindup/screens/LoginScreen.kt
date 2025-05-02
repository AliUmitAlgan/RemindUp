package com.aliumitalgan.remindup.screens


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.aliumitalgan.remindup.utils.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val TAG = "LoginScreen"

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    // Google SignIn Client - try-catch'i Composable dışına taşıyoruz
    val webClientId = context.getString(R.string.web_client_id)
    Log.d(TAG, "Using web client ID: $webClientId")
    val googleSignInClient = remember {
        GoogleAuthHelper.getGoogleSignInClient(context, webClientId)
    }

    // Google SignIn Launcher - try-catch blokları kaldırıldı ve coroutineScope içine alındı
    // Update this part in LoginScreen.kt

// Google SignIn Launcher with improved error handling
    // Separate state for error handling
    var signInErrorMessage by remember { mutableStateOf<String?>(null) }

// Google SignIn Launcher with proper error handling for Compose
    // Google SignIn Launcher with proper error handling for Compose
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google SignIn result received, code: ${result.resultCode}")
        isLoading = true

        if (result.data == null) {
            Log.e(TAG, "Google SignIn result data is null")
            errorMessage = "Google ile giriş başarısız: Veri alınamadı"
            showError = true
            isLoading = false
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        // Use the existing coroutineScope
        coroutineScope.launch {
            try {
                Log.d(TAG, "Processing Google account info...")
                val authResult = GoogleAuthHelper.handleSignInResult(task)

                // Now we're back in a coroutine context, we can safely update UI state
                if (authResult.isSuccess) {
                    Log.d(TAG, "Google sign-in successful")
                    // Since we're already in the coroutineScope, we can directly update UI
                    Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                } else {
                    val error = authResult.exceptionOrNull()
                    Log.e(TAG, "Google sign-in failed: ${error?.message}", error)
                    // Update state values which will trigger recomposition
                    errorMessage = error?.message ?: context.getString(R.string.google_login_fail)
                    showError = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during Google sign-in process: ${e.message}", e)
                // Update state values which will trigger recomposition
                errorMessage = context.getString(R.string.unexpected_error) + ": ${e.message}"
                showError = true
            } finally {
                isLoading = false
            }
        }
    }

    // Hata mesajı gösterimi için LaunchedEffect
    LaunchedEffect(showError) {
        if (showError) {
            // 3 saniye sonra hata mesajını temizle
            kotlinx.coroutines.delay(3000)
            showError = false
        }
    }

    // Check if user is already signed in
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d(TAG, "Kullanıcı zaten giriş yapmış: ${currentUser.email}")
            onLoginSuccess()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    .verticalScroll(rememberScrollState())  // Scroll desteği ekledik
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)  // Elemanlar arasında eşit boşluk
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
                        text = stringResource(R.string.welcome_subtitle),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            stringResource(R.string.login),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(stringResource(R.string.email)) },
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
                            label = { Text(stringResource(R.string.password)) },
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
                                        contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
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

                        // Şifremi Unuttum linki - BURAYA EKLENDİ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    // Şifre sıfırlama işlemi için gerekli işlemleri buraya ekleyebilirsiniz
                                    if (email.isNotEmpty() && email.contains("@")) {
                                        coroutineScope.launch {
                                            try {
                                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                                Toast.makeText(
                                                    context,
                                                    "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } catch (e: Exception) {
                                                errorMessage = "Şifre sıfırlama e-postası gönderilemedi: ${e.message}"
                                                showError = true
                                            }
                                        }
                                    } else {
                                        errorMessage = "Lütfen geçerli bir e-posta adresi girin"
                                        showError = true
                                    }
                                }
                            ) {
                                TextButton(
                                    onClick = { showForgotPasswordDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 16.dp), // Add padding if needed
                                ) {
                                    Text(
                                        text = stringResource(R.string.forgot_password),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End // This will align the text to the end
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Login Button
                        Button(
                            onClick = {
                                if (validateInput(email, password)) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        try {
                                            val result = AuthUtils.loginWithEmail(email, password)
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "Giriş başarılı", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            } else {
                                                errorMessage = result.exceptionOrNull()?.message ?: "Giriş başarısız"
                                                showError = true
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Giriş sırasında hata: ${e.message}"
                                            showError = true
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    errorMessage = "Lütfen geçerli bir email ve şifre girin"
                                    showError = true
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
                                Text(stringResource(R.string.login), fontSize = 16.sp)
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
                        text = stringResource(R.string.or),
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
                        isLoading = true
                        if (googleSignInClient != null) {
                            Log.d(TAG, "Google SignIn başlatılıyor...")
                            // Don't clear cookies or sign out before starting
                            try {
                                GoogleAuthHelper.signIn(googleSignInLauncher, googleSignInClient)
                            } catch (e: Exception) {
                                Log.e(TAG, "Google SignIn error: ${e.message}", e)
                                errorMessage = "Google ile giriş sırasında hata oluştu: ${e.message}"
                                showError = true
                                isLoading = false
                            }
                        } else {
                            Log.e(TAG, "Google SignIn client null")
                            errorMessage = "Google giriş servisi başlatılamadı"
                            showError = true
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && googleSignInClient != null,
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
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
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
                            Text(stringResource(R.string.login_with_google), fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f, fill = true))

                // Register link
                Row(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.no_account),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            stringResource(R.string.register_now),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Şifre Sıfırlama") },
                text = {
                    Column {
                        Text("Şifrenizi sıfırlamak için e-posta adresinizi girin.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = forgotPasswordEmail,
                            onValueChange = { forgotPasswordEmail = it },
                            label = { Text("E-posta") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email")
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (forgotPasswordEmail.isNotEmpty() && forgotPasswordEmail.contains("@")) {
                                // Firebase password reset logic
                                Firebase.auth.sendPasswordResetEmail(forgotPasswordEmail)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            showToast(context, "Şifre sıfırlama bağlantısı e-postanıza gönderildi.")
                                        } else {
                                            showToast(context, "Şifre sıfırlama başarısız: ${task.exception?.message}")
                                        }
                                    }
                                showForgotPasswordDialog = false
                            } else {
                                showToast(context, "Lütfen geçerli bir e-posta adresi girin.")
                            }
                        }
                    ) {
                        Text("Gönder")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }
    }
}


// Input doğrulama
private fun validateInput(email: String, password: String): Boolean {
    return email.contains("@") && email.isNotEmpty() && password.length >= 6
}