package com.aliumitalgan.remindup.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aliumitalgan.remindup.data.local.room.entity.DailyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query(
        """
        SELECT * FROM daily_tasks
        WHERE date = :date
        ORDER BY isCompleted ASC, createdAt ASC
        """
    )
    fun observeTasksByDate(date: String): Flow<List<DailyTaskEntity>>

    @Query(
        """
        SELECT * FROM daily_tasks
        WHERE date = :date
          AND (effort = 'EASY' OR isCompleted = 1)
        ORDER BY isCompleted ASC, createdAt ASC
        """
    )
    fun observeCompassionModeTasksByDate(date: String): Flow<List<DailyTaskEntity>>

    @Query(
        """
        SELECT * FROM daily_tasks
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date ASC, createdAt ASC
        """
    )
    fun observeTasksInRange(
        startDate: String,
        endDate: String
    ): Flow<List<DailyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DailyTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<DailyTaskEntity>)

    @Query(
        """
        UPDATE daily_tasks
        SET isCompleted = :isCompleted,
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE id = :taskId
        """
    )
    suspend fun setCompleted(
        taskId: Long,
        isCompleted: Boolean,
        completedAt: Long?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM daily_tasks WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
