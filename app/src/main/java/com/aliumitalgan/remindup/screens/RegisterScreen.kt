package com.aliumitalgan.remindup.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.AuthUtils
import com.aliumitalgan.remindup.utils.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SignupBackground: Color
    get() = themedColor(Color(0xFFFFF9F7), Color(0xFF0F131A))
private val SignupCard: Color
    get() = themedColor(Color(0xFFFFFFFF), Color(0xFF171D26))
private val SignupTextPrimary: Color
    get() = themedColor(Color(0xFF1F2937), Color(0xFFE5E7EB))
private val SignupTextSecondary: Color
    get() = themedColor(Color(0xFF64748B), Color(0xFFAEB6C5))
private val SignupAccent = Color(0xFFF68B5C)
private val SignupField: Color
    get() = themedColor(Color(0xFFF1F5F9), Color(0xFF232B37))
private val SignupBorder: Color
    get() = themedColor(Color(0xFFE8ECF3), Color(0xFF2E3847))
private val SignupMint: Color
    get() = themedColor(Color(0xFFE8F5E9), Color(0xFF1A2A22))
private val SignupLavender: Color
    get() = themedColor(Color(0xFFF3E5F5), Color(0xFF2A1E32))
private val SignupDangerContainer: Color
    get() = themedColor(Color(0xFFFFE6E6), Color(0xFF3B1F24))
private val SignupDangerText: Color
    get() = themedColor(Color(0xFFB93838), Color(0xFFFFB4B4))

@Composable
fun RegisterScreenContent(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tag = "RegisterScreen"

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            delay(3500)
            errorMessage = ""
        }
    }

    val webClientId = context.getString(R.string.web_client_id)
    val googleSignInClient = remember {
        GoogleAuthHelper.getGoogleSignInClient(context, webClientId)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleLoading = true
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        coroutineScope.launch {
            try {
                val authResult = GoogleAuthHelper.handleSignInResult(task)
                if (authResult.isSuccess) {
                    Toast.makeText(context, context.getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                    onRegisterSuccess()
                } else {
                    val error = authResult.exceptionOrNull()?.localizedMessage
                    errorMessage = error ?: context.getString(R.string.error_google_sign_in)
                }
            } catch (e: Exception) {
                Log.e(tag, "Google auth failed: ${e.message}", e)
                errorMessage = context.getString(R.string.google_auth_error)
            } finally {
                isGoogleLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignupBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .offset(x = 70.dp, y = (-60).dp)
                .background(SignupMint.copy(alpha = 0.62f), CircleShape)
                .blur(72.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(180.dp)
                .offset(x = (-70).dp, y = 50.dp)
                .background(SignupLavender.copy(alpha = 0.58f), CircleShape)
                .blur(72.dp)
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SignupCard),
            border = BorderStroke(1.dp, SignupAccent.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SignupAccent.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = SignupAccent,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = "Join the Sweet Life!",
                    color = SignupTextPrimary,
                    fontSize = 32.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 18.dp)
                )

                Text(
                    text = "Start your journey with RemindUp today",
                    color = SignupTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 22.dp)
                )

                AnimatedVisibility(
                    visible = errorMessage.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = SignupDangerContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            color = SignupDangerText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SignupLabeledField(
                        label = "Full Name",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Enter your full name",
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )

                    SignupLabeledField(
                        label = "Email Address",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Enter your email",
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )

                    SignupPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        visible = passwordVisible,
                        onToggleVisible = { passwordVisible = !passwordVisible }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SignupAccent,
                                uncheckedColor = SignupAccent.copy(alpha = 0.35f),
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            text = "I agree to the ",
                            color = SignupTextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Terms & Conditions",
                            color = SignupAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val validationError = validateInput(
                                name = name,
                                email = email,
                                password = password,
                                acceptedTerms = acceptedTerms,
                                emptyFieldsError = context.getString(R.string.error_empty_fields),
                                invalidEmailError = context.getString(R.string.error_invalid_email),
                                passwordLengthError = context.getString(R.string.error_password_length)
                            )
                            if (validationError != null) {
                                errorMessage = validationError
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                val result = AuthUtils.registerWithEmail(
                                    email = email.trim(),
                                    password = password,
                                    name = name.trim()
                                )
                                isLoading = false

                                if (result.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.register_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onRegisterSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.localizedMessage
                                        ?: context.getString(R.string.register_failed)
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SignupAccent)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 2.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SignupBorder,
                        thickness = 1.dp
                    )
                    Text(
                        text = "OR SIGN UP WITH",
                        color = SignupTextSecondary.copy(alpha = 0.76f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SignupBorder,
                        thickness = 1.dp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isGoogleLoading = true
                            try {
                                GoogleAuthHelper.signIn(googleSignInLauncher, googleSignInClient)
                            } catch (e: Exception) {
                                Log.e(tag, "Google sign in start failed: ${e.message}", e)
                                errorMessage = context.getString(R.string.google_start_error)
                                isGoogleLoading = false
                            }
                        },
                        enabled = !isGoogleLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SignupBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SignupCard,
                            contentColor = SignupTextPrimary
                        )
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = SignupAccent
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "iOS sign up is coming soon.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SignupBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SignupCard,
                            contentColor = SignupTextPrimary
                        )
                    ) {
                        Text(
                            text = "iOS",
                            color = SignupTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 26.dp)
                ) {
                    Text(
                        text = "Already have an account?",
                        color = SignupTextSecondary,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Log In",
                            color = SignupAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignupLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = SignupTextPrimary.copy(alpha = 0.85f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = SignupTextSecondary.copy(alpha = 0.55f),
                    fontSize = 15.sp
                )
            },
            shape = RoundedCornerShape(20.dp),
            colors = signupFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            )
        )
    }
}

@Composable
private fun SignupPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Password",
            color = SignupTextPrimary.copy(alpha = 0.85f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = "Create a password",
                    color = SignupTextSecondary.copy(alpha = 0.55f),
                    fontSize = 15.sp
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisible) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = SignupTextSecondary.copy(alpha = 0.7f)
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = signupFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun signupFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = SignupField,
    unfocusedContainerColor = SignupField,
    disabledContainerColor = SignupField,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = SignupAccent,
    focusedTextColor = SignupTextPrimary,
    unfocusedTextColor = SignupTextPrimary
)

private fun validateInput(
    name: String,
    email: String,
    password: String,
    acceptedTerms: Boolean,
    emptyFieldsError: String,
    invalidEmailError: String,
    passwordLengthError: String
): String? {
    if (name.isBlank() || email.isBlank() || password.isBlank()) {
        return emptyFieldsError
    }
    if (!email.contains("@")) {
        return invalidEmailError
    }
    if (password.length < 6) {
        return passwordLengthError
    }
    if (!acceptedTerms) {
        return "Please agree to the Terms & Conditions."
    }
    return null
}
