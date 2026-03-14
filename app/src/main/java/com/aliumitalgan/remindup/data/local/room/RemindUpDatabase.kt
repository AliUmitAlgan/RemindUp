package com.aliumitalgan.remindup.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aliumitalgan.remindup.data.local.room.converter.RoomConverters
import com.aliumitalgan.remindup.data.local.room.dao.CategoryDao
import com.aliumitalgan.remindup.data.local.room.dao.DailyTaskDao
import com.aliumitalgan.remindup.data.local.room.dao.GoalDao
import com.aliumitalgan.remindup.data.local.room.dao.GoalTemplateDao
import com.aliumitalgan.remindup.data.local.room.entity.CategoryEntity
import com.aliumitalgan.remindup.data.local.room.entity.DailyTaskEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateTaskEntity

@Database(
    entities = [
        CategoryEntity::class,
        GoalEntity::class,
        DailyTaskEntity::class,
        GoalTemplateEntity::class,
        GoalTemplateTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class RemindUpDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun goalDao(): GoalDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun goalTemplateDao(): GoalTemplateDao

    companion object {
        const val DATABASE_NAME: String = "remindup_local.db"

        @Volatile
        private var instance: RemindUpDatabase? = null

        fun getInstance(context: Context): RemindUpDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RemindUpDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
