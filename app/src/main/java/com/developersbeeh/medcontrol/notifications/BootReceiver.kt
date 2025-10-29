// src/main/java/com/developersbeeh/medcontrol/notifications/BootReceiver.kt
package com.developersbeeh.medcontrol.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.di.NotificationReceiverEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "BootReceiver"

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Dispositivo reiniciado, verificando alarmes para reagendar.")

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val hiltEntryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        NotificationReceiverEntryPoint::class.java
                    )
                    val repository = hiltEntryPoint.getFirestoreRepository()
                    val medicationRepository = hiltEntryPoint.getMedicationRepository()
                    // ✅ OBTÉM A INSTÂNCIA DO REPOSITÓRIO CORRETO
                    val reminderRepository = hiltEntryPoint.getReminderRepository()
                    val scheduler = NotificationScheduler(context.applicationContext)
                    val userPrefs = UserPreferences(context.applicationContext)
                    val localDependentId = userPrefs.getDependentId()

                    // Reagendamento para o CUIDADOR logado
                    if (repository.getCurrentUserId() != null && localDependentId == null) {
                        Log.d(TAG, "Cuidador logado. Buscando dependentes para reagendar...")
                        val dependents = repository.getDependentes().first()
                        for (dependent in dependents) {
                            val doseHistory = medicationRepository.getDoseHistory(dependent.id).first()

                            // Reagendar medicamentos
                            val medicamentos = medicationRepository.getMedicamentos(dependent.id).first()
                            medicamentos.forEach { med ->
                                if (med.usaNotificacao && !med.isPaused) {
                                    scheduler.schedule(med, dependent.id, dependent.nome, doseHistory)
                                }
                            }
                            // ✅ USA O REPOSITÓRIO CORRETO
                            val reminders = reminderRepository.getReminders(dependent.id).first()
                            reminders.forEach { reminder ->
                                if(reminder.isActive) {
                                    scheduler.scheduleReminder(reminder, dependent.id)
                                }
                            }
                        }
                        Log.i(TAG, "${dependents.size} dependentes verificados para reagendamento.")
                    }
                    // Reagendamento para o DEPENDENTE logado
                    else if (localDependentId != null) {
                        Log.d(TAG, "Aparelho vinculado ao dependente ID: $localDependentId. Buscando dados...")

                        val dependent = repository.getDependente(localDependentId)
                        if(dependent != null) {
                            val doseHistory = medicationRepository.getDoseHistory(localDependentId).first()
                            val medicamentos = medicationRepository.getMedicamentos(localDependentId).first()
                            medicamentos.forEach { medicamento ->
                                if (medicamento.usaNotificacao && !medicamento.isPaused) {
                                    scheduler.schedule(medicamento, localDependentId, dependent.nome, doseHistory)
                                }
                            }
                            // ✅ USA O REPOSITÓRIO CORRETO
                            val reminders = reminderRepository.getReminders(localDependentId).first()
                            reminders.forEach { reminder ->
                                if(reminder.isActive) {
                                    scheduler.scheduleReminder(reminder, localDependentId)
                                }
                            }
                            Log.i(TAG, "Dados do dependente ${dependent.nome} reagendados.")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Falha ao reagendar notificações: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}