// src/main/java/com/developersbeeh/medcontrol/ui/listmedicamentos/DoseRegistrationEvent.kt
package com.developersbeeh.medcontrol.ui.listmedicamentos

import com.developersbeeh.medcontrol.data.model.Medicamento
import java.time.LocalDateTime

sealed class DoseRegistrationEvent {
    data class ShowLocationSelector(
        val medicamento: Medicamento,
        val proximoLocalSugerido: String?,
        val quantidade: Double?,
        val glicemia: Double? = null,
        val notas: String? = null
    ) : DoseRegistrationEvent()

    data class ShowManualDoseInput(val medicamento: Medicamento) : DoseRegistrationEvent()
    data class ShowCalculatedDoseInput(val medicamento: Medicamento) : DoseRegistrationEvent()
    data class ShowEarlyDoseReasonDialog(
        val medicamento: Medicamento,
        val nextDoseTimeToCancel: LocalDateTime
    ) : DoseRegistrationEvent()
}