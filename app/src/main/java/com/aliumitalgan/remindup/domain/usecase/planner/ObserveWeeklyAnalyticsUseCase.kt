package com.aliumitalgan.remindup.domain.usecase.planner

import com.aliumitalgan.remindup.domain.model.planner.DailyTask
import com.aliumitalgan.remindup.domain.repository.planner.PlannerRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveWeeklyAnalyticsUseCase(
    private val plannerRepository: PlannerRepository
) {
    operator fun invoke(
        weekStart: LocalDate,
        locale: Locale = Locale.getDefault()
    ): Flow<WeeklyAnalyticsSnapshot> {
        val weekEnd = weekStart.plusDays(6)
        return plannerRepository.observeDailyTasksInRange(
            startDate = weekStart,
            endDate = weekEnd
        ).map { tasks ->
            tasks.toWeeklySnapshot(weekStart = weekStart, locale = locale)
        }
    }
}

data class WeeklyAnalyticsSnapshot(
    val progressBars: List<DailyCompletionCount>,
    val completedThisWeek: List<DailyTask>
)

data class DailyCompletionCount(
    val dayLabel: String,
    val completedCount: Int
)

private fun List<DailyTask>.toWeeklySnapshot(
    weekStart: LocalDate,
    locale: Locale
): WeeklyAnalyticsSnapshot {
    val completed = filter { it.isCompleted }
        .sortedByDescending { it.completedAt ?: it.updatedAt }

    val countsByDate = completed.groupBy { it.dateIso }
        .mapValues { (_, tasks) -> tasks.size }

    val progress = (0L..6L).map { offset ->
        val date = weekStart.plusDays(offset)
        val dayLabel = date.dayOfWeek.toShortLabel(locale)
        DailyCompletionCount(
            dayLabel = dayLabel,
            completedCount = countsByDate[date.toString()] ?: 0
        )
    }

    return WeeklyAnalyticsSnapshot(
        progressBars = progress,
        completedThisWeek = completed
    )
}

private fun DayOfWeek.toShortLabel(locale: Locale): String {
    return getDisplayName(TextStyle.SHORT, locale)
}
