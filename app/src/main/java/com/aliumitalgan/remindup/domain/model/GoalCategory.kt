package com.aliumitalgan.remindup.domain.model

data class GoalCategory(
    val id: String = "",
    val name: String = "",
    val colorHex: String = "#FFDAB9",
    val iconKey: String = "self_care",
    val smartRemindersEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

