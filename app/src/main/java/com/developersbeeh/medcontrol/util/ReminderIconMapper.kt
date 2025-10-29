package com.developersbeeh.medcontrol.util

import com.developersbeeh.medcontrol.R

object ReminderIconMapper {
    fun getIconForType(reminderType: String): Int {
        return when {
            reminderType.contains("água", ignoreCase = true) -> R.drawable.ic_water_drop
            reminderType.contains("pressão", ignoreCase = true) -> R.drawable.ic_blood_pressure
            reminderType.contains("exercitar", ignoreCase = true) -> R.drawable.ic_fitness
            reminderType.contains("vitaminas", ignoreCase = true) -> R.drawable.ic_pill
            reminderType.contains("glicose", ignoreCase = true) -> R.drawable.ic_blood_glucose
            else -> R.drawable.ic_alarm
        }
    }
}