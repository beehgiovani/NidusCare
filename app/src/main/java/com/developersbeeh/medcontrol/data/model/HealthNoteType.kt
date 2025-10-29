package com.developersbeeh.medcontrol.data.model

enum class HealthNoteType(val displayName: String) {
    BLOOD_PRESSURE("Pressão Arterial"),
    BLOOD_SUGAR("Glicemia"),
    WEIGHT("Peso"),
    TEMPERATURE("Temperatura"),
    // --- NOVOS TIPOS ADICIONADOS ---
    MOOD("Registro de Humor"),
    SYMPTOM("Registro de Sintoma"),
    GENERAL("Anotação Geral")
}