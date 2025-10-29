// src/main/java/com/developersbeeh/medcontrol/notifications/MotivationalNotificationWorker.kt

package com.developersbeeh.medcontrol.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MotivationalMessageRepository
import com.developersbeeh.medcontrol.data.repository.ProfileCategory
import com.developersbeeh.medcontrol.util.AgeCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private const val TAG = "MotivationalWorker"

@HiltWorker
class MotivationalNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreRepository: FirestoreRepository,
    private val messageRepository: MotivationalMessageRepository,
    private val userPreferences: UserPreferences // Injeção de dependência
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando verificação para envio de notificação motivacional.")

        // Verifica a preferência do usuário ANTES de prosseguir
        if (!userPreferences.isMotivationalNotificationsEnabled()) {
            Log.i(TAG, "Notificações motivacionais desativadas pelo usuário. Trabalho cancelado.")
            return Result.success()
        }

        try {
            val scheduler = NotificationScheduler(applicationContext)
            val dependents = firestoreRepository.getDependentes().first()

            if (dependents.isEmpty()) {
                Log.d(TAG, "Nenhum dependente encontrado. Trabalho concluído.")
                return Result.success()
            }

            dependents.forEach { dependent ->
                val age = AgeCalculator.calculateAge(dependent.dataDeNascimento)
                val category = when {
                    age == null -> ProfileCategory.GENERAL
                    age <= 12 -> ProfileCategory.CHILD
                    age >= 60 -> ProfileCategory.SENIOR
                    else -> ProfileCategory.GENERAL
                }

                val message = messageRepository.getRandomMessageForProfile(category)
                val title = "Um Lembrete Amigo para ${dependent.nome}"
                val notificationId = "motivational_${dependent.id}".hashCode()

                scheduler.showMotivationalNotification(title, message, notificationId)
                Log.i(TAG, "Notificação motivacional enviada para ${dependent.nome} (Categoria: $category)")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao enviar notificação motivacional.", e)
            return Result.retry()
        }
    }

    private fun calculateAge(dataNascimento: String): Int? {
        if (dataNascimento.isBlank()) return null
        return try {
            val birthDate = LocalDate.parse(dataNascimento, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            Period.between(birthDate, LocalDate.now()).years
        } catch (e: DateTimeParseException) {
            try {
                val birthDate = LocalDate.parse(dataNascimento, DateTimeFormatter.ISO_LOCAL_DATE)
                Period.between(birthDate, LocalDate.now()).years
            } catch (e2: DateTimeParseException) {
                null
            }
        }
    }
}