package com.aliumitalgan.remindup.domain.model

data class Goal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val progress: Int = 0,
    val category: Int = 0,
    val isImportant: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
