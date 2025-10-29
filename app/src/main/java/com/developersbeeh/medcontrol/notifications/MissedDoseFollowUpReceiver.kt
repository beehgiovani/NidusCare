// src/main/java/com/developersbeeh/medcontrol/notifications/MissedDoseFollowUpReceiver.kt

package com.developersbeeh.medcontrol.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.di.NotificationReceiverEntryPoint
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

private const val TAG = "MissedDoseFollowUp"

class MissedDoseFollowUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medId = intent.getStringExtra("MED_ID") ?: return@launch
                val dependentId = intent.getStringExtra("DEP_ID") ?: return@launch
                val dependentName = intent.getStringExtra("DEP_NAME") ?: ""
                val followUpCount = intent.getIntExtra("FOLLOW_UP_COUNT", 0)

                if (followUpCount >= 3) { // Limita a 3 repetições
                    Log.w(TAG, "Limite de acompanhamento de dose perdida atingido para med: $medId")
                    return@launch
                }

                val hiltEntryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    NotificationReceiverEntryPoint::class.java
                )
                val medicationRepository = hiltEntryPoint.getMedicationRepository()
                val scheduler = NotificationScheduler(context.applicationContext)

                val medicamento = medicationRepository.getMedicamento(dependentId, medId)
                if (medicamento == null || medicamento.isPaused) {
                    scheduler.cancelMissedDoseFollowUp(medId) // Cancela se o med foi excluído ou pausado
                    return@launch
                }

                val lastScheduledTime = DoseTimeCalculator.getLastScheduledDoseBeforeNow(medicamento) ?: return@launch
                val doseHistory = medicationRepository.getDoseHistory(dependentId).first()

                // Verifica se a dose já foi tomada
                val wasTaken = doseHistory.any {
                    val diff = Duration.between(lastScheduledTime, it.timestamp).toMinutes()
                    diff in -10..60 // Janela de tolerância
                }

                if (!wasTaken) {
                    Log.i(TAG, "Acompanhamento: Dose de '${medicamento.nome}' ainda está perdida. Enviando nova notificação.")
                    // Envia notificação de acompanhamento
                    scheduler.showMissedDoseFollowUpNotification(medicamento, dependentName)
                    // Reagenda o próximo acompanhamento
                    scheduler.scheduleMissedDoseFollowUp(medicamento, dependentId, dependentName, followUpCount + 1)
                } else {
                    Log.i(TAG, "Acompanhamento: Dose de '${medicamento.nome}' foi registrada. Cancelando acompanhamentos.")
                    // Se a dose foi tomada, cancela futuros acompanhamentos para este med
                    scheduler.cancelMissedDoseFollowUp(medId)
                }

            } finally {
                pendingResult.finish()
            }
        }
    }
}