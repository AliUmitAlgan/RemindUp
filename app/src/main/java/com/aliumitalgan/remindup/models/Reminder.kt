package com.aliumitalgan.remindup.models

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val time: String = "",
    val category: ReminderCategory = ReminderCategory.GENERAL,
    val type: ReminderType = ReminderType.SINGLE,
    val description: String = "",
    val userId: String = "",
    val isEnabled: Boolean = true,
    val isImportant: Boolean = false,
    val triggerType: ReminderTriggerType = ReminderTriggerType.TIME,
    val locationTrigger: LocationTrigger? = null,
    val wifiTrigger: WifiTrigger? = null
)

enum class ReminderCategory {
    GENERAL,
    WORK,
    HEALTH,
    PERSONAL,
    STUDY,
    FITNESS
}

enum class ReminderType {
    SINGLE,
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class ReminderTriggerType {
    TIME,
    LOCATION,
    WIFI
}

data class LocationTrigger(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 200f,
    val label: String = ""
)

data class WifiTrigger(
    val ssid: String = ""
)
