package com.aliumitalgan.remindup.domain.model.planner

enum class EnergyLevel(
    val label: String,
    val focusSprintMinutes: Int
) {
    GREAT(label = "Süperim", focusSprintMinutes = 25),
    NORMAL(label = "Normal", focusSprintMinutes = 25),
    TIRED(label = "Yorgunum", focusSprintMinutes = 15)
}
