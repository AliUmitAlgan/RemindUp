package com.aliumitalgan.remindup.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.core.di.AppContainer
import com.aliumitalgan.remindup.domain.usecase.reminder.AddReminderUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.DeleteReminderUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.GetRemindersUseCase
import com.aliumitalgan.remindup.domain.usecase.reminder.UpdateReminderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState(isLoading = true))
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        refreshReminders()
    }

    fun onEvent(event: RemindersUiEvent) {
        when (event) {
            RemindersUiEvent.Load -> refreshReminders()
            is RemindersUiEvent.SearchQueryChanged -> updateAndFilter {
                it.copy(searchQuery = event.value)
            }
            is RemindersUiEvent.CategoryFilterChanged -> updateAndFilter {
                it.copy(selectedCategory = event.value)
            }
            is RemindersUiEvent.TypeFilterChanged -> updateAndFilter {
                it.copy(selectedType = event.value)
            }
            RemindersUiEvent.ToggleSearch -> {
                updateAndFilter {
                    val active = !it.isSearchActive
                    it.copy(
                        isSearchActive = active,
                        searchQuery = if (active) it.searchQuery else ""
                    )
                }
            }
            RemindersUiEvent.ClearFilters -> {
                updateAndFilter {
                    it.copy(
                        searchQuery = "",
                        selectedCategory = null,
                        selectedType = null,
                        isSearchActive = false
                    )
                }
            }
            RemindersUiEvent.ShowAddDialog -> {
                _uiState.update {
                    it.copy(showDialog = true, editingReminderId = null)
                }
            }
            is RemindersUiEvent.ShowEditDialog -> {
                _uiState.update {
                    it.copy(showDialog = true, editingReminderId = event.reminderId)
                }
            }
            RemindersUiEvent.DismissDialog -> {
                _uiState.update {
                    it.copy(showDialog = false, editingReminderId = null)
                }
            }
            is RemindersUiEvent.SaveReminder -> saveReminder(event)
            is RemindersUiEvent.ToggleEnabled -> toggleEnabled(event.reminderId, event.enabled)
            is RemindersUiEvent.DeleteReminder -> deleteReminder(event.reminderId)
            RemindersUiEvent.ClearMessage -> {
                _uiState.update { it.copy(message = null) }
            }
        }
    }

    private fun saveReminder(event: RemindersUiEvent.SaveReminder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val editingId = _uiState.value.editingReminderId
            val result = if (editingId != null) {
                updateReminderUseCase(editingId, event.reminder)
            } else {
                addReminderUseCase(event.reminder).map { }
            }

            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        showDialog = false,
                        editingReminderId = null,
                        message = if (editingId != null) {
                            "Hatırlatıcı güncellendi."
                        } else {
                            "Hatırlatıcı eklendi."
                        }
                    )
                }
                refreshReminders()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Hatırlatıcı kaydedilemedi."
                    )
                }
            }
        }
    }

    private fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            deleteReminderUseCase(reminderId)
                .onSuccess {
                    _uiState.update { it.copy(message = "Hatırlatıcı silindi.") }
                    refreshReminders()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = error.message ?: "Hatırlatıcı silinemedi."
                        )
                    }
                }
        }
    }

    private fun toggleEnabled(reminderId: String, enabled: Boolean) {
        viewModelScope.launch {
            val target = _uiState.value.allReminders.firstOrNull { it.id == reminderId } ?: return@launch
            val updated = target.reminder.copy(isEnabled = enabled)
            updateReminderUseCase(reminderId, updated)
                .onSuccess {
                    refreshReminders()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(message = error.message ?: "Hatırlatıcı durumu güncellenemedi.")
                    }
                }
        }
    }

    private fun refreshReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getRemindersUseCase()
                .onSuccess { reminders ->
                    _uiState.update { current ->
                        val updated = current.copy(
                            isLoading = false,
                            allReminders = reminders
                        )
                        updated.copy(visibleReminders = applyFilters(updated))
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allReminders = emptyList(),
                            visibleReminders = emptyList(),
                            message = error.message ?: "Hatırlatıcılar yüklenemedi."
                        )
                    }
                }
        }
    }

    private fun updateAndFilter(transform: (RemindersUiState) -> RemindersUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(visibleReminders = applyFilters(updated))
        }
    }

    private fun applyFilters(state: RemindersUiState) = state.allReminders
        .asSequence()
        .filter { item ->
            state.selectedCategory?.let { item.reminder.category == it } ?: true
        }
        .filter { item ->
            state.selectedType?.let { item.reminder.type == it } ?: true
        }
        .filter { item ->
            val query = state.searchQuery.trim()
            if (query.isBlank()) {
                true
            } else {
                item.reminder.title.contains(query, ignoreCase = true) ||
                    item.reminder.description.contains(query, ignoreCase = true)
            }
        }
        .sortedBy { it.reminder.time }
        .toList()
}

class RemindersViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = RemindersViewModel(
            getRemindersUseCase = appContainer.getRemindersUseCase,
            addReminderUseCase = appContainer.addReminderUseCase,
            updateReminderUseCase = appContainer.updateReminderUseCase,
            deleteReminderUseCase = appContainer.deleteReminderUseCase
        )
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
