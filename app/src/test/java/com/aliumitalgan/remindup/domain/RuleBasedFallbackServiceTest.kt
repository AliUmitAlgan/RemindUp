package com.aliumitalgan.remindup.domain

import com.aliumitalgan.remindup.domain.service.RuleBasedFallbackService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedFallbackServiceTest {

    private val service = RuleBasedFallbackService()

    @Test
    fun `generateTaskBreakdown returns steps for non-empty goal`() {
        val result = service.generateTaskBreakdown("Vizelere çalış")

        assertFalse(result.subtasks.isEmpty())
        assertTrue(result.subtasks.size >= 4)
    }

    @Test
    fun `rankTasksByEnergy keeps all tasks`() {
        val input = listOf("Short", "This is a significantly longer task title")
        val ranked = service.rankTasksByEnergy(input)

        assertTrue(ranked.containsAll(input))
        assertTrue(ranked.size == input.size)
    }
}
