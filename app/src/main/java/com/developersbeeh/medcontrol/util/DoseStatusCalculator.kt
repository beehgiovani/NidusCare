package com.developersbeeh.medcontrol.util

import android.util.Log
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.FrequenciaTipo
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.RecordedDoseStatus
import com.developersbeeh.medcontrol.data.model.TipoMedicamento
import com.developersbeeh.medcontrol.ui.caregiver.ProximaDoseStatus
import com.developersbeeh.medcontrol.ui.listmedicamentos.AdherenceStatus
import com.developersbeeh.medcontrol.ui.listmedicamentos.MedicamentoStatus
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ✅ CORREÇÃO: Adicionado o status 'SKIPPED' que estava faltando
enum class CalculatedDoseStatus {
    ON_TIME,
    TOO_EARLY,
    TOO_LATE,
    EXTRA,
    SPORADIC,
    SKIPPED
}

object DoseStatusCalculator {

    private const val ON_TIME_WINDOW_MINUTES = 30L
    private const val MAX_MATCH_MINUTES = 180L

    fun calculateDoseStatus(
        currentDose: DoseHistory,
        medicamento: Medicamento,
        allDosesForMedication: List<DoseHistory>
    ): CalculatedDoseStatus {
        if (medicamento.isUsoEsporadico) {
            return CalculatedDoseStatus.SPORADIC
        }

        // ✅ CORREÇÃO: Verifica o status salvo no banco de dados primeiro
        if (currentDose.status == RecordedDoseStatus.SKIPPED) {
            return CalculatedDoseStatus.SKIPPED
        }

        val doseDateTime = currentDose.timestamp
        val doseDate = doseDateTime.toLocalDate()

        if (!DoseTimeCalculator.isMedicationDay(medicamento, doseDate)) {
            Log.d("DoseStatusCalculator", "Dose de '${medicamento.nome}' em dia não agendado. Classificada como EXTRA.")
            return CalculatedDoseStatus.EXTRA
        }

        val scheduledTimesToday = medicamento.horarios.sorted()
        if (scheduledTimesToday.isEmpty()) {
            Log.d("DoseStatusCalculator", "Medicamento '${medicamento.nome}' não possui horários. Classificada como EXTRA.")
            return CalculatedDoseStatus.EXTRA
        }

        var closestScheduledDateTime: LocalDateTime? = null
        var minDifference = Long.MAX_VALUE

        for (scheduledTime in scheduledTimesToday) {
            val scheduledDateTime = LocalDateTime.of(doseDate, scheduledTime)
            val difference = Duration.between(scheduledDateTime, doseDateTime).abs().toMinutes()
            if (difference < minDifference) {
                minDifference = difference
                closestScheduledDateTime = scheduledDateTime
            }
        }

        if (closestScheduledDateTime == null || minDifference > MAX_MATCH_MINUTES) {
            Log.d("DoseStatusCalculator", "Nenhum horário agendado próximo para a dose de '${medicamento.nome}'. Classificada como EXTRA.")
            return CalculatedDoseStatus.EXTRA
        }

        val otherDosesOnSameDay = allDosesForMedication.filter {
            it.id != currentDose.id && it.timestamp.toLocalDate() == doseDate && it.status == RecordedDoseStatus.TAKEN
        }

        for (otherDose in otherDosesOnSameDay) {
            val otherDifference = Duration.between(closestScheduledDateTime, otherDose.timestamp).abs().toMinutes()
            if (otherDifference < minDifference) {
                Log.d("DoseStatusCalculator", "Outra dose é uma correspondência melhor para '${medicamento.nome}' às '$closestScheduledDateTime'. Classificada como EXTRA.")
                return CalculatedDoseStatus.EXTRA
            }
        }

        val signedDifference = Duration.between(closestScheduledDateTime, doseDateTime).toMinutes()

        return when {
            signedDifference < -ON_TIME_WINDOW_MINUTES -> CalculatedDoseStatus.TOO_EARLY
            signedDifference > ON_TIME_WINDOW_MINUTES -> CalculatedDoseStatus.TOO_LATE
            else -> CalculatedDoseStatus.ON_TIME
        }
    }
}