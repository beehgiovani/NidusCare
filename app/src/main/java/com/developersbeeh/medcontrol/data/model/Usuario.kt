// src/main/java/com/developersbeeh/medcontrol/data/model/Usuario.kt

package com.developersbeeh.medcontrol.data.model

import com.google.firebase.Timestamp

data class Usuario(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val email_lowercase: String = email.lowercase(),
    val photoUrl: String? = null,
    val sexo: String = "NAO_INFORMADO",
    val tipoSanguineo: String = "NAO_SABE",
    val altura: String = "",
    val peso: String = "",
    val dataDeNascimento: String = "",
    val condicoesPreexistentes: String = "",
    val alergias: String = "",
    val premium: Boolean = false,
    val familyId: String? = null,
    val fcmToken: String? = null,
    val profileIncomplete: Boolean = false,
    val subscriptionExpiryDate: Timestamp? = null,

    // NOMES DE CAMPOS CORRIGIDOS PARA CORRESPONDER AO BACKEND
    val doseRemindersEnabled: Boolean = true,
    val missedDoseAlertsEnabled: Boolean = true,
    val lowStockAlertsEnabled: Boolean = true,
    val expiryAlertsEnabled: Boolean = true,
    val appointmentRemindersEnabled: Boolean = true,
    val vaccineAlertsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false, // Nome antigo 'resumoDiarioAtivado' era inconsistente
    val dailySummaryTime: Int = 8, // Nome antigo 'resumoDiarioHora'
    val motivationalNotificationsEnabled: Boolean = true,
    val hydrationRemindersEnabled: Boolean = true
)