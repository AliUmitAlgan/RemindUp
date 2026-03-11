package com.aliumitalgan.remindup.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.domain.model.EntitlementStatus
import com.aliumitalgan.remindup.domain.model.PlanType
import com.aliumitalgan.remindup.domain.repository.AuthRepository
import com.aliumitalgan.remindup.domain.repository.EntitlementRepository
import com.aliumitalgan.remindup.domain.usecase.goal.GetUserGoalsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val name: String = "Sweet Reminder",
    val email: String = "remindup.user@sweet.com",
    val tasksDone: Int = 128,
    val streaks: Int = 42,
    val awards: Int = 12,
    val isPremium: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SettingsViewModel(
    private val getUserGoalsUseCase: GetUserGoalsUseCase,
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val user = authRepository.getCurrentUser()
            val name = user?.name?.ifBlank { null } ?: "Sweet Reminder"
            val email = user?.email?.ifBlank { null } ?: "remindup.user@sweet.com"

            val computed = withContext(Dispatchers.IO) {
                computeProfileMetrics()
            }

            _uiState.update {
                it.copy(
                    name = name,
                    email = email,
                    tasksDone = computed.tasksDone,
                    streaks = computed.streaks,
                    awards = computed.awards,
                    isPremium = computed.isPremium,
                    isLoading = false,
                    error = computed.error
                )
            }
        }
    }

    private suspend fun computeProfileMetrics(): ComputedProfile {
        var tasksDone = 128
        var streaks = 42
        var awards = 12
        var isPremium = false
        var error: String? = null

        runCatching {
            val entitlement = entitlementRepository.getEntitlement()
            isPremium = entitlement.planType == PlanType.PREMIUM &&
                (entitlement.status == EntitlementStatus.ACTIVE || entitlement.status == EntitlementStatus.GRACE)
        }.onFailure { throwable ->
            error = throwable.message ?: error
        }

        runCatching {
            val goals = getUserGoalsUseCase().getOrDefault(emptyList()).map { it.second }
            if (goals.isNotEmpty()) {
                tasksDone = (goals.sumOf { goal -> goal.progress } / 3).coerceAtLeast(8)
                streaks = goals.count { goal -> goal.progress >= 60 }.coerceAtLeast(1) * 3
                awards = goals.count { goal -> goal.progress >= 100 }.coerceAtLeast(1) * 2
            }
        }.onFailure { throwable ->
            if (error == null) {
                error = throwable.message
            }
        }

        return ComputedProfile(
            tasksDone = tasksDone,
            streaks = streaks,
            awards = awards,
            isPremium = isPremium,
            error = error
        )
    }
}

private data class ComputedProfile(
    val tasksDone: Int,
    val streaks: Int,
    val awards: Int,
    val isPremium: Boolean,
    val error: String?
)
