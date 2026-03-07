package com.aliumitalgan.remindup.core.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.aliumitalgan.remindup.data.repository.FirestoreReminderRepository
import com.aliumitalgan.remindup.data.repository.BillingVerificationRepository
import com.aliumitalgan.remindup.data.repository.FirestoreEntitlementRepository
import com.aliumitalgan.remindup.data.repository.FunctionAiAssistantRepository
import com.aliumitalgan.remindup.data.scheduler.WorkManagerReminderScheduler
import com.aliumitalgan.remindup.domain.repository.ReminderRepository
import com.aliumitalgan.remindup.domain.service.RuleBasedFallbackService
import com.aliumitalgan.remindup.domain.usecase.reminder.AddReminderUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.DeleteReminderUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.GetRemindersUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.UpdateReminderUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(
    context: Context
) {
    private val appContext = context.applicationContext

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val entitlementRepository by lazy {
        FirestoreEntitlementRepository(
            auth = auth,
            firestore = firestore
        )
    }

    val aiAssistantRepository by lazy { FunctionAiAssistantRepository() }
    val fallbackService by lazy { RuleBasedFallbackService() }
    val billingVerificationRepository by lazy { BillingVerificationRepository() }

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

    val getRemindersUseCase by lazy { GetRemindersUseCase(reminderRepository) }
    val addReminderUseCase by lazy { AddReminderUseCase(reminderRepository) }
    val updateReminderUseCase by lazy { UpdateReminderUseCase(reminderRepository) }
    val deleteReminderUseCase by lazy { DeleteReminderUseCase(reminderRepository) }

    fun appContext(): Context = appContext
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided.")
}
