// src/main/java/com/developersbeeh/medcontrol/ui/achievements/AchievementsViewModel.kt
package com.developersbeeh.medcontrol.ui.achievements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.repository.*
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _achievements = MutableLiveData<List<Achievement>>()
    val achievements: LiveData<List<Achievement>> = _achievements

    private lateinit var dependentId: String

    fun initialize(id: String) {
        if (this::dependentId.isInitialized && dependentId == id) return
        dependentId = id
        loadAndProcessAchievements()
    }

    private fun loadAndProcessAchievements() {
        viewModelScope.launch {
            // 1. Pega a lista "mestra" de todas as conquistas possíveis
            val masterList = AchievementsList.getAllAchievements()

            // 2. Busca todos os dados relevantes do usuário em paralelo
            val userLogs = fetchUserLogs()

            // 3. Calcula o progresso para cada conquista
            masterList.forEach { achievement ->
                calculateProgress(achievement, userLogs)
            }

            // 4. Ordena a lista (desbloqueadas primeiro) e a envia para a UI
            val sortedList = masterList.sortedWith(compareBy({ it.state }, { it.id.ordinal }))
            _achievements.postValue(sortedList)
        }
    }

    private fun calculateProgress(achievement: Achievement, logs: UserLogs) {
        var progress = 0
        when (achievement.id) {
            // Primeiros Passos
            AchievementId.FIRST_DOSE -> progress = if (logs.doseHistory.isNotEmpty()) 1 else 0
            AchievementId.FIRST_NOTE -> progress = if (logs.healthNotes.isNotEmpty()) 1 else 0
            // Adicionar lógica para FIRST_WELLBEING_LOG, ADD_DEPENDENT quando os repositórios estiverem prontos

            // Adesão (lógica simplificada, pode ser aprimorada)
            AchievementId.ADHERENCE_STREAK_7 -> progress = logs.adherenceStreak.coerceAtMost(7)
            AchievementId.ADHERENCE_STREAK_30 -> progress = logs.adherenceStreak.coerceAtMost(30)
            AchievementId.PERFECT_WEEK -> {
                // Lógica complexa: verificar se a aderência foi 100% nos últimos 7 dias.
                // Por simplicidade, vamos usar a aderência geral dos últimos 7 dias.
                val adherence7d = DoseTimeCalculator.calcularAderencia7dias(logs.medications, logs.doseHistory)
                progress = if (adherence7d >= 100) 7 else 0 // Simplificado
            }

            // Registros
            AchievementId.LOG_10_DOSES -> progress = logs.doseHistory.size.coerceAtMost(10)
            AchievementId.LOG_50_DOSES -> progress = logs.doseHistory.size.coerceAtMost(50)
            AchievementId.LOG_100_DOSES -> progress = logs.doseHistory.size.coerceAtMost(100)
            AchievementId.LOG_10_NOTES -> progress = logs.healthNotes.size.coerceAtMost(10)
            AchievementId.LOG_5_WEIGHTS -> progress = logs.healthNotes.count { it.type == com.developersbeeh.medcontrol.data.model.HealthNoteType.WEIGHT }.coerceAtMost(5)

            // IA e Ferramentas (requer salvar flags no UserPreferences ou logs de atividade)
            AchievementId.USE_AI_CHAT -> progress = if(logs.activityLogs.any { it.tipo == com.developersbeeh.medcontrol.data.model.TipoAtividade.AI_CHAT_USED }) 1 else 0

            else -> {
                // Conquistas ainda não implementadas
            }
        }

        achievement.currentProgress = progress
        if (achievement.currentProgress >= achievement.targetValue) {
            achievement.state = AchievementState.UNLOCKED
        }
    }

    private suspend fun fetchUserLogs(): UserLogs {
        // Esta função poderia ser otimizada para buscar apenas os dados necessários
        val medsFlow = medicationRepository.getMedicamentos(dependentId)
        val dosesFlow = medicationRepository.getDoseHistory(dependentId)
        val notesFlow = firestoreRepository.getHealthNotes(dependentId)
        val activityFlow = activityLogRepository.getActivityLogs(dependentId)
        // Adicionar outros fluxos aqui (hidratação, etc.)

        // Combina os fluxos para garantir que temos todos os dados antes de prosseguir
        return combine(medsFlow, dosesFlow, notesFlow, activityFlow) { meds, doses, notes, activities ->
            UserLogs(
                medications = meds,
                doseHistory = doses,
                healthNotes = notes,
                activityLogs = activities,
                adherenceStreak = 0 // TODO: Buscar do UserPreferences
            )
        }.first()
    }
}

// Data class auxiliar para passar os dados do usuário
data class UserLogs(
    val medications: List<com.developersbeeh.medcontrol.data.model.Medicamento>,
    val doseHistory: List<DoseHistory>,
    val healthNotes: List<HealthNote>,
    val activityLogs: List<com.developersbeeh.medcontrol.data.model.Atividade>,
    val adherenceStreak: Int
)