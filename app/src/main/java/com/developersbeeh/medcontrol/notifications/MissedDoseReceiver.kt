// src/main/java/com/developersbeeh/medcontrol/notifications/MissedDoseReceiver.kt
package com.developersbeeh.medcontrol.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.developersbeeh.medcontrol.di.NotificationReceiverEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

private const val TAG = "MissedDoseReceiver"

class MissedDoseReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MED_ID = "MED_ID"
        const val EXTRA_DEP_ID = "DEP_ID"
        const val EXTRA_DEP_NAME = "DEP_NAME"
        const val EXTRA_SCHEDULED_TIME = "SCHEDULED_TIME"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medId = intent.getStringExtra(EXTRA_MED_ID) ?: return@launch
                val dependentId = intent.getStringExtra(EXTRA_DEP_ID) ?: return@launch
                val dependentName = intent.getStringExtra(EXTRA_DEP_NAME) ?: ""
                val scheduledTimeStr = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: return@launch
                val scheduledTime = LocalDateTime.parse(scheduledTimeStr)

                val hiltEntryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    NotificationReceiverEntryPoint::class.java
                )
                val medicationRepository = hiltEntryPoint.getMedicationRepository()
                val scheduler = NotificationScheduler(context.applicationContext)

                val medicamento = medicationRepository.getMedicamento(dependentId, medId)
                if (medicamento == null || medicamento.isPaused) {
                    return@launch
                }

                val doseHistory = medicationRepository.getDoseHistory(dependentId).first()
                val wasTaken = doseHistory.any {
                    Duration.between(scheduledTime, it.timestamp).abs().toMinutes() < 30
                }

                if (!wasTaken) {
                    Log.i(TAG, "Dose de '${medicamento.nome}' está atrasada. Exibindo notificação inteligente.")
                    scheduler.showMissedDoseNotification(medicamento, dependentId, dependentName, scheduledTime)
                }

            } finally {
                pendingResult.finish()
            }
        }
    }
}