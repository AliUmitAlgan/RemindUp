package com.aliumitalgan.remindup.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

enum class BiometricCapability {
    Available,
    NoHardware,
    HardwareUnavailable,
    NoneEnrolled,
    Unknown
}

data class BiometricAuthOutcome(
    val success: Boolean,
    val cancelled: Boolean = false,
    val errorMessage: String? = null
)

class BiometricAuthManager(
    private val context: Context
) {
    private val biometricManager: BiometricManager = BiometricManager.from(context)

    fun getCapability(): BiometricCapability {
        return when (
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NoneEnrolled
            else -> BiometricCapability.Unknown
        }
    }

    fun capabilityMessage(capability: BiometricCapability): String {
        return when (capability) {
            BiometricCapability.Available -> "Use your fingerprint or face to protect app access."
            BiometricCapability.NoHardware -> "Biometric hardware is not available on this device."
            BiometricCapability.HardwareUnavailable -> "Biometric hardware is temporarily unavailable."
            BiometricCapability.NoneEnrolled -> "No biometric credential is enrolled on this device."
            BiometricCapability.Unknown -> "Biometric status is unavailable."
        }
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String
    ): BiometricAuthOutcome = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) {
                        continuation.resume(BiometricAuthOutcome(success = true))
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!continuation.isActive) return

                    val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED

                    continuation.resume(
                        BiometricAuthOutcome(
                            success = false,
                            cancelled = cancelled,
                            errorMessage = errString.toString()
                        )
                    )
                }

                override fun onAuthenticationFailed() {
                    // Keep the prompt open; BiometricPrompt will invoke success/error callbacks.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(promptInfo)
    }
}
