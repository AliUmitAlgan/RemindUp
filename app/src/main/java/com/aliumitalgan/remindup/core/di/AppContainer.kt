package com.aliumitalgan.remindup.core.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aliumitalgan.remindup.data.repository.BillingVerificationRepository
import com.aliumitalgan.remindup.data.repository.FirebaseAuthRepository
import com.aliumitalgan.remindup.data.repository.FirestoreGoalCategoryRepository
import com.aliumitalgan.remindup.data.repository.FirestoreEntitlementRepository
import com.aliumitalgan.remindup.data.repository.FirestoreGoalRepository
import com.aliumitalgan.remindup.data.repository.FirestoreReminderRepository
import com.aliumitalgan.remindup.data.repository.FunctionAiAssistantRepository
import com.aliumitalgan.remindup.data.repository.SocialRepository
import com.aliumitalgan.remindup.data.scheduler.WorkManagerReminderScheduler
import com.aliumitalgan.remindup.domain.repository.AuthRepository
import com.aliumitalgan.remindup.domain.repository.GoalCategoryRepository
import com.aliumitalgan.remindup.domain.repository.GoalRepository
import com.aliumitalgan.remindup.domain.repository.ReminderRepository
import com.aliumitalgan.remindup.domain.service.RuleBasedFallbackService
import com.aliumitalgan.remindup.domain.usecase.goal.AddGoalUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.DeleteGoalUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.GetUserGoalsUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.UpdateGoalProgressUseCase
import com.aliumitalgan.remindup.domain.usecase.goalcategory.DeleteGoalCategoryUseCase
import com.aliumitalgan.remindup.domain.usecase.goalcategory.GetGoalCategoriesUseCase
import com.aliumitalgan.remindup.domain.usecase.goalcategory.GetGoalCategoryByIdUseCase
import com.aliumitalgan.remindup.domain.usecase.goalcategory.SaveGoalCategoryUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.AddReminderUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.DeleteReminderUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.GetRemindersUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.UpdateReminderUseCase
import com.aliumitalgan.remindup.presentation.auth.LoginViewModel
import com.aliumitalgan.remindup.presentation.category.EditCategoryViewModel
import com.aliumitalgan.remindup.presentation.goals.GoalsViewModel
import com.aliumitalgan.remindup.presentation.home.HomeViewModel
import com.aliumitalgan.remindup.presentation.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(
    context: Context
) {
    private val appContext = context.applicationContext

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Repositories
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(auth, firestore) }
    val goalRepository: GoalRepository by lazy { FirestoreGoalRepository(auth, firestore) }
    val goalCategoryRepository: GoalCategoryRepository by lazy { FirestoreGoalCategoryRepository(auth, firestore) }
    val entitlementRepository by lazy {
        FirestoreEntitlementRepository(auth = auth, firestore = firestore)
    }
    val aiAssistantRepository by lazy { FunctionAiAssistantRepository() }
    val fallbackService by lazy { RuleBasedFallbackService() }
    val billingVerificationRepository by lazy { BillingVerificationRepository() }
    val socialRepository by lazy { SocialRepository(firestore, auth) }

    private val reminderScheduler by lazy {
        WorkManagerReminderScheduler(context = appContext)
    }

    val reminderRepository: ReminderRepository by lazy {
        FirestoreReminderRepository(
            auth = auth,
            firestore = firestore,
            scheduler = reminderScheduler
        )
    }

    // Goal Use Cases
    val getUserGoalsUseCase by lazy { GetUserGoalsUseCase(goalRepository) }
    val addGoalUseCase by lazy { AddGoalUseCase(goalRepository) }
    val updateGoalProgressUseCase by lazy { UpdateGoalProgressUseCase(goalRepository) }
    val deleteGoalUseCase by lazy { DeleteGoalUseCase(goalRepository) }
    val getGoalCategoriesUseCase by lazy { GetGoalCategoriesUseCase(goalCategoryRepository) }
    val getGoalCategoryByIdUseCase by lazy { GetGoalCategoryByIdUseCase(goalCategoryRepository) }
    val saveGoalCategoryUseCase by lazy { SaveGoalCategoryUseCase(goalCategoryRepository) }
    val deleteGoalCategoryUseCase by lazy { DeleteGoalCategoryUseCase(goalCategoryRepository) }

    // Reminder Use Cases
    val getRemindersUseCase by lazy { GetRemindersUseCase(reminderRepository) }
    val addReminderUseCase by lazy { AddReminderUseCase(reminderRepository) }
    val updateReminderUseCase by lazy { UpdateReminderUseCase(reminderRepository) }
    val deleteReminderUseCase by lazy { DeleteReminderUseCase(reminderRepository) }

    fun appContext(): Context = appContext
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided.")
}

@Suppress("UNCHECKED_CAST")
class RemindUpViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(
                container.authRepository
            ) as T
            modelClass.isAssignableFrom(GoalsViewModel::class.java) -> GoalsViewModel(
                container.getUserGoalsUseCase,
                container.addGoalUseCase,
                container.updateGoalProgressUseCase,
                container.deleteGoalUseCase,
                container.getGoalCategoriesUseCase,
                container.deleteGoalCategoryUseCase
            ) as T
            modelClass.isAssignableFrom(EditCategoryViewModel::class.java) -> EditCategoryViewModel(
                container.getGoalCategoryByIdUseCase,
                container.saveGoalCategoryUseCase
            ) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
                container.getUserGoalsUseCase,
                container.reminderRepository,
                container.authRepository
            ) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
                container.getUserGoalsUseCase,
                container.authRepository,
                container.entitlementRepository
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
