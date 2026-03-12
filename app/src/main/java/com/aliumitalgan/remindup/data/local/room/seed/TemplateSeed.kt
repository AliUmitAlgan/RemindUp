package com.aliumitalgan.remindup.data.local.room.seed

import com.aliumitalgan.remindup.data.local.room.dao.GoalTemplateDao
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateTaskEntity
import com.aliumitalgan.remindup.data.local.room.model.TaskEffort

data class TemplateSeed(
    val name: String,
    val description: String,
    val category: String,
    val iconEmoji: String,
    val tasks: List<TemplateTaskSeed>
)

data class TemplateTaskSeed(
    val title: String,
    val notes: String = "",
    val effort: TaskEffort,
    val defaultDurationMinutes: Int
)

object LocalGoalTemplates {
    val defaults: List<TemplateSeed> = listOf(
        TemplateSeed(
            name = "Sağlıklı Yaşam",
            description = "Günün ritmine uygun, sürdürülebilir ve nazik wellness rutini.",
            category = "Wellness",
            iconEmoji = "🌿",
            tasks = listOf(
                TemplateTaskSeed(
                    title = "1 bardak su iç",
                    effort = TaskEffort.EASY,
                    defaultDurationMinutes = 5
                ),
                TemplateTaskSeed(
                    title = "15 dakika yürüyüş",
                    effort = TaskEffort.EASY,
                    defaultDurationMinutes = 15
                ),
                TemplateTaskSeed(
                    title = "10 dakika esneme",
                    effort = TaskEffort.MEDIUM,
                    defaultDurationMinutes = 10
                )
            )
        ),
        TemplateSeed(
            name = "Dijital Detoks",
            description = "Ekran süresini azaltıp odak ve sakinlik kazandıran plan.",
            category = "Mindfulness",
            iconEmoji = "📵",
            tasks = listOf(
                TemplateTaskSeed(
                    title = "Bildirimleri sessize al",
                    effort = TaskEffort.EASY,
                    defaultDurationMinutes = 5
                ),
                TemplateTaskSeed(
                    title = "45 dakika telefonsuz çalışma",
                    effort = TaskEffort.MEDIUM,
                    defaultDurationMinutes = 45
                ),
                TemplateTaskSeed(
                    title = "Yatmadan 30 dakika önce ekranı bırak",
                    effort = TaskEffort.HARD,
                    defaultDurationMinutes = 30
                )
            )
        )
    )
}

suspend fun seedDefaultTemplates(dao: GoalTemplateDao) {
    if (dao.templateCount() > 0) return

    LocalGoalTemplates.defaults.forEach { template ->
        val insertedId = dao.insertTemplate(
            GoalTemplateEntity(
                name = template.name,
                description = template.description,
                category = template.category,
                iconEmoji = template.iconEmoji
            )
        )
        val templateId = if (insertedId > 0L) insertedId else dao.getTemplateIdByName(template.name)
        if (templateId != null) {
            dao.insertTemplateTasks(
                template.tasks.mapIndexed { index, task ->
                    GoalTemplateTaskEntity(
                        templateId = templateId,
                        title = task.title,
                        notes = task.notes,
                        effort = task.effort,
                        defaultDurationMinutes = task.defaultDurationMinutes,
                        sortOrder = index
                    )
                }
            )
        }
    }
}
