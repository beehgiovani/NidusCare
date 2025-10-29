package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ✅ REATORADO: Enums agora são apenas chaves, sem 'displayName'.
enum class FlowIntensity {
    NONE,
    SPOTTING,
    LIGHT,
    MEDIUM,
    HEAVY
}

enum class Symptom {
    CRAMPS,
    HEADACHE,
    BLOATING,
    FATIGUE,
    MOOD_SWINGS,
    ACNE,
    TENDER_BREASTS,
    BACK_PAIN,
    NAUSEA,
    CRAVINGS
}

enum class Mood {
    HAPPY,
    CALM,
    SAD,
    IRRITABLE,
    ANXIOUS
}

enum class CervicalMucus {
    DRY,
    STICKY,
    CREAMY,
    EGG_WHITE
}

data class DailyCycleLog(
    @DocumentId
    val dateString: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),

    val dependentId: String = "",
    val flow: FlowIntensity = FlowIntensity.NONE,
    val symptoms: List<Symptom> = emptyList(), // Mantido como List
    val mood: Mood? = null,
    val hadSexualActivity: Boolean = false,
    val wasProtected: Boolean? = null,
    val cervicalMucus: CervicalMucus? = null,
    val basalBodyTemperature: Double? = null,
    val notes: String? = null
) {
    fun getDate(): LocalDate = LocalDate.parse(dateString)
}