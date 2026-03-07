package com.aliumitalgan.remindup.domain.repository

import com.aliumitalgan.remindup.domain.model.SnoozeCoachingResult
import com.aliumitalgan.remindup.domain.model.TaskBreakdownResult

interface AiAssistantRepository {
    suspend fun generateTaskBreakdown(goal: String, locale: String): TaskBreakdownResult
    suspend fun getSnoozeCoaching(taskTitle: String, snoozeCount: Int, locale: String): SnoozeCoachingResult
    suspend fun rankTasksByEnergy(tasks: List<String>, locale: String): List<String>
}
