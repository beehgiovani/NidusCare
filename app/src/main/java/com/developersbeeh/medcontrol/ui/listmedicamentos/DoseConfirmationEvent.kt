// src/main/java/com/developersbeeh/medcontrol/ui/listmedicamentos/DoseConfirmationEvent.kt
package com.developersbeeh.medcontrol.ui.listmedicamentos

import com.developersbeeh.medcontrol.data.model.Medicamento
import java.time.LocalDateTime

/**
 * Representa eventos que exigem uma confirmação do usuário antes de uma dose ser registrada.
 */
sealed class DoseConfirmationEvent {
    // Pergunta se o usuário quer registrar uma dose um pouco adiantada.
    data class ConfirmSlightlyEarlyDose(val medicamento: Medicamento, val nextDoseTime: LocalDateTime) : DoseConfirmationEvent()

    // Pede uma justificativa para registrar uma dose muito adiantada.
    data class ConfirmVeryEarlyDose(val medicamento: Medicamento, val nextDoseTime: LocalDateTime, val noteRequired: Boolean) : DoseConfirmationEvent()

    // Alerta sobre o registro de uma dose extra, além do esperado para o dia.
    data class ConfirmExtraDose(val medicamento: Medicamento) : DoseConfirmationEvent()

    // Pergunta se o usuário quer registrar uma dose de um medicamento de uso esporádico.
    data class ConfirmSporadicDose(val medicamento: Medicamento) : DoseConfirmationEvent()

    // Pergunta se o usuário quer reajustar os horários após registrar uma dose muito atrasada.
    data class ConfirmLateDose(val medicamento: Medicamento, val lateDoseTime: LocalDateTime, val hoursLate: Long) : DoseConfirmationEvent()

    // ✅ NOVO EVENTO: Pergunta se o usuário tomou a dose atrasada agora ou se apenas esqueceu de registrar.
    data class ConfirmLateDoseLogging(val medicamento: Medicamento, val scheduledTime: LocalDateTime) : DoseConfirmationEvent()
}