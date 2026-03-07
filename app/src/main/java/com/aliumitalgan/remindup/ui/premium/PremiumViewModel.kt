package com.aliumitalgan.remindup.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.core.di.AppContainer
import com.aliumitalgan.remindup.data.repository.BillingVerificationRepository
import com.aliumitalgan.remindup.domain.model.PlanType
import com.aliumitalgan.remindup.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PremiumViewModel(
    private val entitlementRepository: EntitlementRepository,
    private val billingVerificationRepository: BillingVerificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        refreshEntitlement()
    }

    fun onEvent(event: PremiumUiEvent) {
        when (event) {
            is PremiumUiEvent.OffersLoaded -> {
                _uiState.value = _uiState.value.copy(offers = event.offers, isLoading = false)
            }

            is PremiumUiEvent.PurchaseVerified -> verifyPurchase(event.purchaseToken, event.productId)
            is PremiumUiEvent.BillingError -> {
                _uiState.value = _uiState.value.copy(errorMessage = event.message, isLoading = false)
            }
            PremiumUiEvent.RestorePurchases -> {
                _uiState.value = _uiState.value.copy(infoMessage = "Checking previous purchases...")
            }
            PremiumUiEvent.Refresh -> refreshEntitlement()
            PremiumUiEvent.ClearMessages -> {
                _uiState.value = _uiState.value.copy(infoMessage = null, errorMessage = null)
            }
        }
    }

    private fun refreshEntitlement() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val entitlement = entitlementRepository.getEntitlement()
            val config = entitlementRepository.getLimitConfig()
            val label = if (entitlement.planType == PlanType.PREMIUM) "Premium" else "Free"
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentPlanLabel = label,
                infoMessage = if (!config.paidTierEnabled) {
                    "AI features are in waitlist mode due current free-tier limits."
                } else {
                    null
                }
            )
        }
    }

    private fun verifyPurchase(purchaseToken: String, productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val success = runCatching {
                billingVerificationRepository.verifyPurchase(purchaseToken, productId)
            }.getOrDefault(false)

            if (success) {
                refreshEntitlement()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    infoMessage = "Premium enabled."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Purchase verification failed."
                )
            }
        }
    }
}

class PremiumViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = PremiumViewModel(
            entitlementRepository = appContainer.entitlementRepository,
            billingVerificationRepository = appContainer.billingVerificationRepository
        )
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
