// src/main/java/com/developersbeeh/medcontrol/notifications/AdherenceStreakWorker.kt

package com.developersbeeh.medcontrol.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Conquista
import com.developersbeeh.medcontrol.data.model.TipoConquista
import com.developersbeeh.medcontrol.data.repository.AchievementRepository
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private const val TAG = "AdherenceStreakWorker"

@HiltWorker
class AdherenceStreakWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository,
    private val userPreferences: UserPreferences,
    private val achievementRepository: AchievementRepository // Repositório injetado
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando verificação diária de sequência de adesão.")

        try {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            val dependents = firestoreRepository.getDependentes().first()

            for (dependent in dependents) {
                val lastCheckDateStr = userPreferences.getLastAdherenceCheckDate(dependent.id)
                val lastCheckDate = lastCheckDateStr?.let { LocalDate.parse(it) }
                var currentStreak = userPreferences.getAdherenceStreak(dependent.id)

                // Se a última checagem foi antes de anteontem, a sequência foi quebrada.
                if (lastCheckDate != null && lastCheckDate.isBefore(yesterday)) {
                    Log.d(TAG, "Sequência quebrada para ${dependent.nome}. Última checagem em $lastCheckDateStr")
                    currentStreak = 0
                }

                // Só processa se a checagem de ontem ainda não foi feita.
                if (lastCheckDate == null || !lastCheckDate.isEqual(today)) {
                    val meds = medicationRepository.getMedicamentos(dependent.id).first()
                    val history = medicationRepository.getDoseHistory(dependent.id).first()

                    val scheduledMeds = meds.filter { !it.isUsoEsporadico && it.horarios.isNotEmpty() && !it.isPaused }
                    val dosesExpectedYesterday = DoseTimeCalculator.calculateExpectedDosesForPeriod(scheduledMeds, yesterday, yesterday)
                    val dosesTakenYesterday = history.count { it.timestamp.toLocalDate().isEqual(yesterday) }

                    if (dosesExpectedYesterday > 0 && dosesTakenYesterday >= dosesExpectedYesterday) {
                        currentStreak++
                        Log.i(TAG, "Adesão de 100% para ${dependent.nome} ontem. Nova sequência: $currentStreak")

                        // Verifica se a conquista foi alcançada
                        if (currentStreak == 7) {
                            val conquista = Conquista(
                                id = TipoConquista.SETE_DIAS_ADESAO_PERFEITA.name,
                                tipo = TipoConquista.SETE_DIAS_ADESAO_PERFEITA
                            )
                            achievementRepository.awardAchievement(dependent.id, conquista)
                            Log.i(TAG, "CONQUISTA 'SEMANA PERFEITA' concedida para ${dependent.nome}!")
                        }
                    } else if (dosesExpectedYesterday > 0) {
                        Log.w(TAG, "Adesão < 100% para ${dependent.nome} ontem. Sequência zerada.")
                        currentStreak = 0
                    }

                    // Salva a nova sequência e a data da checagem
                    userPreferences.saveAdherenceStreak(dependent.id, currentStreak)
                    userPreferences.saveLastAdherenceCheckDate(dependent.id, today.toString())
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao verificar sequência de adesão.", e)
            return Result.retry()
        }
    }
}