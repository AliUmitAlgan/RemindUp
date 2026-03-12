package com.aliumitalgan.remindup.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateTaskEntity
import com.aliumitalgan.remindup.data.local.room.relation.GoalTemplateWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalTemplateDao {
    @Query("SELECT * FROM goal_templates ORDER BY name ASC")
    fun observeTemplates(): Flow<List<GoalTemplateEntity>>

    @Transaction
    @Query("SELECT * FROM goal_templates WHERE id = :templateId LIMIT 1")
    suspend fun getTemplateWithTasks(templateId: Long): GoalTemplateWithTasks?

    @Query("SELECT id FROM goal_templates WHERE name = :name LIMIT 1")
    suspend fun getTemplateIdByName(name: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplate(template: GoalTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplateTasks(tasks: List<GoalTemplateTaskEntity>)

    @Query("SELECT COUNT(*) FROM goal_templates")
    suspend fun templateCount(): Int
}
