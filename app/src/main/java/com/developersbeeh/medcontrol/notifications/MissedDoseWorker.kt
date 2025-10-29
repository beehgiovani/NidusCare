// src/main/java/com/developersbeeh/medcontrol/notifications/MissedDoseWorker.kt
package com.developersbeeh.medcontrol.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.developersbeeh.medcontrol.data.model.Insight
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "MissedDoseWorker"
private const val MISSED_DOSE_THRESHOLD_MINUTES = 30L

@HiltWorker
class MissedDoseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando verificação periódica de doses perdidas...")
        try {
            val scheduler = NotificationScheduler(applicationContext)
            if (firestoreRepository.getCurrentUserId() == null) {
                return Result.success()
            }
            val dependents = firestoreRepository.getDependentes().first()
            dependents.forEach { dependent ->
                val activeMeds = medicationRepository.getMedicamentos(dependent.id).first()
                    .filter { !it.isPaused && it.usaNotificacao && !it.isUsoEsporadico && it.horarios.isNotEmpty() }
                if (activeMeds.isEmpty()) return@forEach

                val doseHistory = medicationRepository.getDoseHistory(dependent.id).first()

                activeMeds.forEach { med ->
                    if (med.missedDoseAlertSent) return@forEach
                    val lastScheduledTime = DoseTimeCalculator.getLastScheduledDoseBeforeNow(med) ?: return@forEach

                    val minutesSinceScheduled = Duration.between(lastScheduledTime, LocalDateTime.now()).toMinutes()
                    if (minutesSinceScheduled >= MISSED_DOSE_THRESHOLD_MINUTES) {
                        val wasTaken = doseHistory.any {
                            Duration.between(lastScheduledTime, it.timestamp).abs().toMinutes() < MISSED_DOSE_THRESHOLD_MINUTES
                        }
                        if (!wasTaken) {
                            Log.w(TAG, "Dose perdida detectada para '${med.nome}' de ${dependent.nome}!")

                            // ✅ CORREÇÃO: Passando os parâmetros corretos
                            scheduler.showMissedDoseNotification(med, dependent.id, dependent.nome, lastScheduledTime)

                            val updatedMed = med.copy(missedDoseAlertSent = true)
                            medicationRepository.saveMedicamento(dependent.id, updatedMed)

                            // ... (lógica de Insight)

                            // ✅ CORREÇÃO: Adicionando a chamada para agendar o acompanhamento
                            scheduler.scheduleMissedDoseFollowUp(med, dependent.id, dependent.nome, 0)
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}