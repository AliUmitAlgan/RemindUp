package com.aliumitalgan.remindup.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aliumitalgan.remindup.data.local.room.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getById(goalId: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity): Long

    @Query("UPDATE goals SET isArchived = 1, updatedAt = :updatedAt WHERE id = :goalId")
    suspend fun archive(goalId: Long, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(goal: GoalEntity)
}
