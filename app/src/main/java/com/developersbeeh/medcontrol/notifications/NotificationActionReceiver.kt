// src/main/java/com/developersbeeh/medcontrol/notifications/NotificationActionReceiver.kt
package com.developersbeeh.medcontrol.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.developersbeeh.medcontrol.di.NotificationReceiverEntryPoint
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_AS_TAKEN = "com.developersbeeh.medcontrol.ACTION_MARK_AS_TAKEN"
        const val ACTION_SNOOZE = "com.developersbeeh.medcontrol.ACTION_SNOOZE"
        const val ACTION_MARK_AS_TAKEN_NOW = "com.developersbeeh.medcontrol.ACTION_MARK_AS_TAKEN_NOW"
        const val ACTION_SKIP_DOSE = "com.developersbeeh.medcontrol.ACTION_SKIP_DOSE"

        const val EXTRA_MEDICAMENTO_ID = "extra_medicamento_id"
        const val EXTRA_DEPENDENT_ID = "extra_dependent_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_DEPENDENT_NAME = "extra_dependent_name"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
        private const val TAG = "NotificationAction"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Ação de notificação recebida: ${intent.action}")
        when (intent.action) {
            ACTION_MARK_AS_TAKEN -> handleMarkAsTaken(context, intent, isLate = false)
            ACTION_MARK_AS_TAKEN_NOW -> handleMarkAsTaken(context, intent, isLate = true)
            ACTION_SNOOZE -> handleSnooze(context, intent)
            ACTION_SKIP_DOSE -> handleSkipDose(context, intent)
        }
    }

    private fun handleMarkAsTaken(context: Context, intent: Intent, isLate: Boolean) {
        val medicamentoId = intent.getStringExtra(EXTRA_MEDICAMENTO_ID)
        val dependentId = intent.getStringExtra(EXTRA_DEPENDENT_ID)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val dependentName = intent.getStringExtra(EXTRA_DEPENDENT_NAME) ?: ""

        if (medicamentoId == null || dependentId == null) {
            Log.e(TAG, "Erro: IDs nulos ao tentar marcar dose como tomada.")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hiltEntryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    NotificationReceiverEntryPoint::class.java
                )
                val medicationRepository = hiltEntryPoint.getMedicationRepository()
                val medicamento = medicationRepository.getMedicamento(dependentId, medicamentoId)

                if (medicamento != null) {
                    val notas = if (isLate) "Dose registrada com atraso pela notificação." else null
                    medicationRepository.recordDoseAndUpdateStock(dependentId, medicamento, null, null, notas, LocalDateTime.now()).getOrThrow()

                    val scheduler = NotificationScheduler(context.applicationContext)
                    val updatedHistory = medicationRepository.getDoseHistory(dependentId).first()
                    scheduler.schedule(medicamento, dependentId, dependentName, updatedHistory)
                } else {
                    Log.e(TAG, "Medicamento com ID '$medicamentoId' não encontrado.")
                }

                if (notificationId != -1) {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(notificationId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao marcar dose como tomada: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleSkipDose(context: Context, intent: Intent) {
        val medicamentoId = intent.getStringExtra(EXTRA_MEDICAMENTO_ID)
        val dependentId = intent.getStringExtra(EXTRA_DEPENDENT_ID)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val scheduledTimeStr = intent.getStringExtra(EXTRA_SCHEDULED_TIME)

        if (medicamentoId == null || dependentId == null || scheduledTimeStr == null) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hiltEntryPoint = EntryPointAccessors.fromApplication(context.applicationContext, NotificationReceiverEntryPoint::class.java)
                val medicationRepository = hiltEntryPoint.getMedicationRepository()
                val medicamento = medicationRepository.getMedicamento(dependentId, medicamentoId)

                if(medicamento != null){
                    // Registra a dose como "não tomada" com a nota e o horário agendado original
                    val scheduledTime = LocalDateTime.parse(scheduledTimeStr)
                    medicationRepository.recordSkippedDose(dependentId, medicamento, "Dose pulada pela notificação.", scheduledTime)
                }

                if (notificationId != -1) {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(notificationId)
                }
            } catch(e: Exception) {
                Log.e(TAG, "Erro ao pular dose: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleSnooze(context: Context, intent: Intent) {
        val medicamentoId = intent.getStringExtra(EXTRA_MEDICAMENTO_ID)
        val dependentId = intent.getStringExtra(EXTRA_DEPENDENT_ID)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val dependentName = intent.getStringExtra(EXTRA_DEPENDENT_NAME) ?: ""
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 5)

        if (medicamentoId == null || dependentId == null) {
            Log.e(TAG, "Erro: IDs nulos ao tentar adiar notificação.")
            return
        }

        if (notificationId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hiltEntryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    NotificationReceiverEntryPoint::class.java
                )
                val medicationRepository = hiltEntryPoint.getMedicationRepository()
                val medicamento = medicationRepository.getMedicamento(dependentId, medicamentoId)

                if (medicamento != null) {
                    Log.d(TAG, "Adiando notificação para '${medicamento.nome}' por $snoozeMinutes minutos.")
                    val scheduler = NotificationScheduler(context.applicationContext)
                    scheduler.scheduleSnooze(medicamento, dependentId, dependentName, snoozeMinutes.toLong())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adiar notificação: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}