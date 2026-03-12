package com.aliumitalgan.remindup.domain.usecase.planner

import com.aliumitalgan.remindup.domain.model.planner.DailyTask
import com.aliumitalgan.remindup.domain.model.planner.EnergyLevel
import com.aliumitalgan.remindup.domain.repository.planner.PlannerRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class ObserveTodayDailyTasksUseCase(
    private val plannerRepository: PlannerRepository
) {
    operator fun invoke(
        date: LocalDate,
        energyLevel: EnergyLevel
    ): Flow<List<DailyTask>> {
        return plannerRepository.observeTodayDailyTasks(
            date = date,
            easyOnly = energyLevel == EnergyLevel.TIRED
        )
    }
}
