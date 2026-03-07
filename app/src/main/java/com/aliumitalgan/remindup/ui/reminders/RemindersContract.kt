package com.aliumitalgan.remindup.ui.reminders

import com.aliumitalgan.remindup.domain.model.ReminderRecord
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.models.ReminderType

data class RemindersUiState(
    val isLoading: Boolean = false,
    val allReminders: List<ReminderRecord> = emptyList(),
    val visibleReminders: List<ReminderRecord> = emptyList(),
    val selectedCategory: ReminderCategory? = null,
    val selectedType: ReminderType? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val showDialog: Boolean = false,
    val editingReminderId: String? = null,
    val message: String? = null
)

sealed interface RemindersUiEvent {
    data object Load : RemindersUiEvent
    data class SearchQueryChanged(val value: String) : RemindersUiEvent
    data class CategoryFilterChanged(val value: ReminderCategory?) : RemindersUiEvent
    data class TypeFilterChanged(val value: ReminderType?) : RemindersUiEvent
    data object ToggleSearch : RemindersUiEvent
    data object ClearFilters : RemindersUiEvent

    data object ShowAddDialog : RemindersUiEvent
    data class ShowEditDialog(val reminderId: String) : RemindersUiEvent
    data object DismissDialog : RemindersUiEvent
    data class SaveReminder(val reminder: Reminder) : RemindersUiEvent

    data class ToggleEnabled(val reminderId: String, val enabled: Boolean) : RemindersUiEvent
    data class DeleteReminder(val reminderId: String) : RemindersUiEvent
    data object ClearMessage : RemindersUiEvent
}
