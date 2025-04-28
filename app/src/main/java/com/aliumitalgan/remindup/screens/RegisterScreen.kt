package com.aliumitalgan.remindup.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.utils.AnimationUtils
import com.aliumitalgan.remindup.utils.AuthUtils
import kotlinx.coroutines.launch

@Composable
fun RegisterScreenContent(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AnimationUtils.FadeAnimation(visible = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Kayıt Ol",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ad Soyad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Şifre Tekrar") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                                showToast(context, "Kayıt başarısız: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
                            }
                        }
                    } else {
                        showToast(context, "Lütfen tüm alanları doğru şekilde doldurun")
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Kayıt Ol")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onNavigateToLogin
            ) {
                Text(
                    text = "Zaten hesabınız var mı? Giriş yapın",
                    color = MaterialTheme.colorScheme.primary
                )
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