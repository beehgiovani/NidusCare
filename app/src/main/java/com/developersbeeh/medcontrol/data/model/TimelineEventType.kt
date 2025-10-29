package com.developersbeeh.medcontrol.data.model

/**
 * Tipos de eventos da timeline.
 */
enum class TimelineEventType(val value: String) {
    DOSE_TAKEN("DOSE_TAKEN"),
    DOSE_SKIPPED("DOSE_SKIPPED"),
    DOSE_MISSED("DOSE_MISSED"),
    APPOINTMENT("APPOINTMENT"),
    DOCUMENT_ADDED("DOCUMENT_ADDED"),
    MEDICATION_ADDED("MEDICATION_ADDED"),
    MEDICATION_UPDATED("MEDICATION_UPDATED"),
    MEDICATION_ARCHIVED("MEDICATION_ARCHIVED"),
    GENERIC("GENERIC");

    companion object {
        fun fromString(value: String): TimelineEventType {
            return values().find { it.value == value } ?: GENERIC
        }
    }
}

