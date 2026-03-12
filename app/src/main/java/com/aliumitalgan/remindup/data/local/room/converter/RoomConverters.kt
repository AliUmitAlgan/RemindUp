package com.aliumitalgan.remindup.data.local.room.converter

import androidx.room.TypeConverter
import com.aliumitalgan.remindup.data.local.room.model.TaskEffort

class RoomConverters {
    @TypeConverter
    fun fromTaskEffort(value: TaskEffort): String = value.name

    @TypeConverter
    fun toTaskEffort(value: String): TaskEffort {
        return TaskEffort.entries.firstOrNull { it.name == value } ?: TaskEffort.MEDIUM
    }
}
