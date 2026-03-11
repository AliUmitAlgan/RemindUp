package com.aliumitalgan.remindup.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.utils.BiometricCapability
import com.aliumitalgan.remindup.utils.UserPreferenceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityUiState(
    val isLoading: Boolean = true,
    val isUpdatingBiometric: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricCapability: BiometricCapability = BiometricCapability.Unknown,
    val biometricStatusText: String = "Checking biometric availability...",
    val errorMessage: String? = null
) {
    val isBiometricAvailable: Boolean
        get() = biometricCapability == BiometricCapability.Available
}

class SecurityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        refreshSecurityPreferences()
    }

    fun refreshSecurityPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            UserPreferenceUtils.getSecurityPreferences()
                .onSuccess { prefs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            biometricEnabled = prefs.biometricEnabled,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            biometricEnabled = false,
                            errorMessage = error.message ?: "Failed to load security settings."
                        )
                    }
                }
        }
    }

    fun updateBiometricCapability(
        capability: BiometricCapability,
        statusText: String
    ) {
        _uiState.update {
            it.copy(
                biometricCapability = capability,
                biometricStatusText = statusText
            )
        }
    }

    fun updateBiometricEnabled(nextValue: Boolean) {
        val previous = _uiState.value.biometricEnabled
        _uiState.update {
            it.copy(
                biometricEnabled = nextValue,
                isUpdatingBiometric = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            UserPreferenceUtils.updateSecurityPreference(
                field = "biometricEnabled",
                value = nextValue
            ).onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isUpdatingBiometric = false,
                        biometricEnabled = nextValue
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isUpdatingBiometric = false,
                        biometricEnabled = previous,
                        errorMessage = error.message ?: "Unable to update biometric setting."
                    )
                }
            }
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

