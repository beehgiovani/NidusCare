// src/main/java/com/developersbeeh/medcontrol/notifications/HydrationReminderWorker.kt

package com.developersbeeh.medcontrol.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil

private const val TAG = "HydrationReminderWorker"

@HiltWorker
class HydrationReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreRepository: FirestoreRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando verificação de hidratação inteligente.")

        // Roda apenas durante o dia (ex: 8h às 22h)
        val now = LocalTime.now()
        if (now.hour < 8 || now.hour > 22) {
            Log.d(TAG, "Fora do horário de notificação. Trabalho adiado.")
            return Result.success()
        }

        try {
            val scheduler = NotificationScheduler(applicationContext)
            val dependents = firestoreRepository.getDependentes().first()

            dependents.forEach { dependent ->
                val hidratacaoMeta = dependent.metaHidratacaoMl
                if (hidratacaoMeta <= 0) return@forEach // Ignora se não há meta

                val historicoHoje = firestoreRepository.getHidratacaoHistory(dependent.id, LocalDate.now()).first()
                val totalConsumido = historicoHoje.sumOf { it.quantidadeMl }

                // Se a meta já foi atingida, não faz nada
                if (totalConsumido >= hidratacaoMeta) {
                    Log.i(TAG, "Meta de hidratação para ${dependent.nome} já foi atingida. Nenhuma notificação necessária.")
                    return@forEach
                }

                val restante = hidratacaoMeta - totalConsumido
                val horasRestantes = (22 - now.hour).coerceAtLeast(1) // Horas até as 22h

                val metaPorHora = restante.toDouble() / horasRestantes
                val sugestaoDeConsumo = (ceil(metaPorHora / 50) * 50).toInt() // Arredonda para o próximo múltiplo de 50

                if (sugestaoDeConsumo > 100) { // Envia notificação apenas se a sugestão for significativa
                    val title = "Lembrete de Hidratação para ${dependent.nome}"
                    val message = "Você está um pouco atrás da sua meta hoje! Que tal beber cerca de ${sugestaoDeConsumo}ml na próxima hora para ficar em dia?"
                    val notificationId = "hydration_reminder_${dependent.id}".hashCode()

                    scheduler.showHydrationReminderNotification(title, message, notificationId)
                    Log.i(TAG, "Notificação de hidratação inteligente enviada para ${dependent.nome}.")
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao executar o HydrationReminderWorker.", e)
            return Result.retry()
        }
    }
}