package com.developersbeeh.medcontrol.ui.timeline

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import java.time.LocalDate
import java.time.LocalDateTime

enum class TimelineItemCategory {
    DOSE, NOTE, ACTIVITY, INSIGHT,
    WELLBEING
}

data class TimelineLogItem(
    val id: String,
    val timestamp: LocalDateTime,
    val description: String,
    val author: String,
    @DrawableRes val iconRes: Int,
    @ColorRes val iconTintRes: Int,
    val category: TimelineItemCategory,
    val noteType: HealthNoteType? = null
)

sealed interface TimelineListItem {
    data class HeaderItem(val date: LocalDate) : TimelineListItem
    data class LogItem(val log: TimelineLogItem) : TimelineListItem
}