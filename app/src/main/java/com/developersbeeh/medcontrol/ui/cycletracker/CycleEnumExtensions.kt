package com.developersbeeh.medcontrol.ui.cycletracker

import android.content.Context
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.*

// Este arquivo ajuda a converter os Enums em Strings legíveis usando os recursos do app

fun FlowIntensity.getDisplayName(context: Context): String {
    val resId = when (this) {
        FlowIntensity.NONE -> R.string.flow_intensity_none
        FlowIntensity.SPOTTING -> R.string.flow_intensity_spotting
        FlowIntensity.LIGHT -> R.string.flow_intensity_light
        FlowIntensity.MEDIUM -> R.string.flow_intensity_medium
        FlowIntensity.HEAVY -> R.string.flow_intensity_heavy
    }
    return context.getString(resId)
}

fun Symptom.getDisplayName(context: Context): String {
    val resId = when (this) {
        Symptom.CRAMPS -> R.string.symptom_cramps
        Symptom.HEADACHE -> R.string.symptom_headache
        Symptom.BLOATING -> R.string.symptom_bloating
        Symptom.FATIGUE -> R.string.symptom_fatigue
        Symptom.MOOD_SWINGS -> R.string.symptom_mood_swings
        Symptom.ACNE -> R.string.symptom_acne
        Symptom.TENDER_BREASTS -> R.string.symptom_tender_breasts
        Symptom.BACK_PAIN -> R.string.symptom_back_pain
        Symptom.NAUSEA -> R.string.symptom_nausea
        Symptom.CRAVINGS -> R.string.symptom_cravings
    }
    return context.getString(resId)
}

fun Mood.getDisplayName(context: Context): String {
    val resId = when (this) {
        Mood.HAPPY -> R.string.mood_happy
        Mood.CALM -> R.string.mood_calm
        Mood.SAD -> R.string.mood_sad
        Mood.IRRITABLE -> R.string.mood_irritable
        Mood.ANXIOUS -> R.string.mood_anxious
    }
    return context.getString(resId)
}

fun CervicalMucus.getDisplayName(context: Context): String {
    val resId = when (this) {
        CervicalMucus.DRY -> R.string.mucus_dry
        CervicalMucus.STICKY -> R.string.mucus_sticky
        CervicalMucus.CREAMY -> R.string.mucus_creamy
        CervicalMucus.EGG_WHITE -> R.string.mucus_egg_white
    }
    return context.getString(resId)
}