package com.aliumitalgan.remindup.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val showCurrentPassword: Boolean = false,
    val showNewPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val hasMinLength: Boolean
        get() = newPassword.length >= 8

    val hasSpecialCharacter: Boolean
        get() = newPassword.any { !it.isLetterOrDigit() }

    val canSubmit: Boolean
        get() = currentPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            !isUpdating
}

class ChangePasswordViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun updateCurrentPassword(value: String) {
        _uiState.update {
            it.copy(
                currentPassword = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun updateNewPassword(value: String) {
        _uiState.update {
            it.copy(
                newPassword = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.update { it.copy(showCurrentPassword = !it.showCurrentPassword) }
    }

    fun toggleNewPasswordVisibility() {
        _uiState.update { it.copy(showNewPassword = !it.showNewPassword) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(showConfirmPassword = !it.showConfirmPassword) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun submitPasswordChange() {
        val state = _uiState.value
        if (!state.canSubmit || state.isUpdating) return

        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "You need to log in again to change password.") }
            return
        }

        _uiState.update {
            it.copy(
                isUpdating = true,
                errorMessage = null,
                successMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val credential = EmailAuthProvider.getCredential(email, state.currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(state.newPassword).await()

                _uiState.update {
                    it.copy(
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        isUpdating = false,
                        errorMessage = null,
                        successMessage = "Password updated successfully."
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        errorMessage = mapAuthError(error)
                    )
                }
            }
        }
    }

    private fun validate(state: ChangePasswordUiState): String? {
        if (!state.hasMinLength) return "New password must be at least 8 characters."
        if (!state.hasSpecialCharacter) return "New password must include at least one special character."
        if (state.newPassword != state.confirmPassword) return "New password and confirmation do not match."
        if (state.newPassword == state.currentPassword) return "New password must be different from current password."
        return null
    }

    private fun mapAuthError(error: Exception): String {
        return when (error) {
            is FirebaseAuthInvalidCredentialsException -> "Current password is incorrect."
            is FirebaseAuthRecentLoginRequiredException -> "Please log in again and retry."
            else -> error.message ?: "Failed to update password."
        }
    }
}
