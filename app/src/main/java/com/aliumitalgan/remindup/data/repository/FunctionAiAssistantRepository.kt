package com.aliumitalgan.remindup.data.repository

import com.aliumitalgan.remindup.domain.model.AiResponseSource
import com.aliumitalgan.remindup.domain.model.SnoozeCoachingResult
import com.aliumitalgan.remindup.domain.model.TaskBreakdownResult
import com.aliumitalgan.remindup.domain.repository.AiAssistantRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FunctionAiAssistantRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : AiAssistantRepository {

    override suspend fun generateTaskBreakdown(goal: String, locale: String): TaskBreakdownResult {
        val payload = mapOf(
            "taskTitle" to goal,
            "locale" to locale
        )
        val result = functions
            .getHttpsCallable("generateTaskBreakdown")
            .call(payload)
            .await()

        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?> ?: emptyMap()
        @Suppress("UNCHECKED_CAST")
        val subtasks = data["subtasks"] as? List<String> ?: emptyList()
        val source = if ((data["source"] as? String)?.equals("model", ignoreCase = true) == true) {
            AiResponseSource.MODEL
        } else {
            AiResponseSource.FALLBACK
        }

        return TaskBreakdownResult(
            subtasks = subtasks,
            source = source,
            message = data["message"] as? String
        )
    }

    override suspend fun getSnoozeCoaching(
        taskTitle: String,
        snoozeCount: Int,
        locale: String
    ): SnoozeCoachingResult {
        val payload = mapOf(
            "taskTitle" to taskTitle,
            "snoozeCount" to snoozeCount,
            "locale" to locale
        )
        val result = functions
            .getHttpsCallable("getSnoozeCoaching")
            .call(payload)
            .await()

        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?> ?: emptyMap()
        @Suppress("UNCHECKED_CAST")
        val actions = data["actions"] as? List<String> ?: emptyList()
        val source = if ((data["source"] as? String)?.equals("model", ignoreCase = true) == true) {
            AiResponseSource.MODEL
        } else {
            AiResponseSource.FALLBACK
        }

        return SnoozeCoachingResult(
            message = data["message"] as? String ?: "",
            actions = actions,
            source = source
        )
    }

    override suspend fun rankTasksByEnergy(tasks: List<String>, locale: String): List<String> {
        val payload = mapOf(
            "tasks" to tasks,
            "locale" to locale
        )
        val result = functions
            .getHttpsCallable("rankTasksByEnergyWindow")
            .call(payload)
            .await()

        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?> ?: emptyMap()
        @Suppress("UNCHECKED_CAST")
        return data["orderedTaskIds"] as? List<String> ?: tasks
    }
}
