package com.aliumitalgan.remindup.domain.usecase.reminder

import com.aliumitalgan.remindup.domain.model.ReminderRecord
import com.aliumitalgan.remindup.domain.repository.ReminderRepository
import com.aliumitalgan.remindup.models.Reminder

class GetRemindersUseCase(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(): Result<List<ReminderRecord>> = repository.getUserReminders()
}

class AddReminderUseCase(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): Result<String> = repository.addReminder(reminder)
}

class UpdateReminderUseCase(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: String, reminder: Reminder): Result<Unit> {
        return repository.updateReminder(reminderId, reminder)
    }
}

class DeleteReminderUseCase(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: String): Result<Unit> = repository.deleteReminder(reminderId)
}
