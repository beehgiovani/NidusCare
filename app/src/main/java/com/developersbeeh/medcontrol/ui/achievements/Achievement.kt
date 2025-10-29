// src/main/java/com/developersbeeh/medcontrol/ui/achievements/Achievement.kt
package com.developersbeeh.medcontrol.ui.achievements

import androidx.annotation.DrawableRes
import com.developersbeeh.medcontrol.R

// Enum para controlar o estado visual de uma conquista
enum class AchievementState {
    LOCKED,
    UNLOCKED
}

// Enum para identificar cada conquista de forma única
enum class AchievementId {
    // Primeiros Passos
    FIRST_DOSE,
    FIRST_NOTE,
    FIRST_WELLBEING_LOG,
    ADD_DEPENDENT,

    // Adesão
    ADHERENCE_STREAK_7,
    ADHERENCE_STREAK_30,
    PERFECT_WEEK, // 100% de adesão em 7 dias

    // Registros
    LOG_10_DOSES,
    LOG_50_DOSES,
    LOG_100_DOSES,
    LOG_10_NOTES,
    LOG_5_WEIGHTS,

    // Bem-Estar
    HYDRATION_GOAL_7_DAYS,
    ACTIVITY_GOAL_7_DAYS,
    LOG_ALL_WELLBEING_IN_ONE_DAY,

    // Funcionalidades Avançadas
    USE_AI_CHAT,
    USE_PREDICTIVE_ANALYSIS,
    USE_RECIPE_SCANNER,
    USE_MEAL_ANALYSIS,
    CREATE_FIRST_REPORT,

    // Social
    INVITE_CAREGIVER
}

// A nova data class que representa uma conquista
data class Achievement(
    val id: AchievementId,
    val title: String,
    val description: String,
    @DrawableRes val unlockedIcon: Int,
    val targetValue: Int,
    var currentProgress: Int = 0,
    var state: AchievementState = AchievementState.LOCKED
)

// Objeto que contém a "master list" de todas as conquistas do aplicativo
object AchievementsList {
    fun getAllAchievements(): List<Achievement> {
        return listOf(
            // Primeiros Passos
            Achievement(AchievementId.FIRST_DOSE, "Primeira Dose", "Registre sua primeira dose de medicamento.", R.drawable.ic_check, 1),
            Achievement(AchievementId.FIRST_NOTE, "Primeira Anotação", "Faça sua primeira anotação de saúde.", R.drawable.ic_notes, 1),
            Achievement(AchievementId.FIRST_WELLBEING_LOG, "Diário Inaugurado", "Faça seu primeiro registro no Diário de Bem-Estar.", R.drawable.ic_wellbeing, 1),
            Achievement(AchievementId.ADD_DEPENDENT, "Círculo de Cuidado", "Adicione seu primeiro dependente.", R.drawable.ic_add_person, 1),

            // Adesão
            Achievement(AchievementId.ADHERENCE_STREAK_7, "Sequência de 7 Dias", "Mantenha uma sequência de adesão por 7 dias.", R.drawable.ic_whatshot, 7),
            Achievement(AchievementId.ADHERENCE_STREAK_30, "Mestre da Adesão", "Mantenha uma sequência de adesão por 30 dias.", R.drawable.ic_local_fire_department, 30),
            Achievement(AchievementId.PERFECT_WEEK, "Semana Perfeita", "Atinja 100% de adesão aos medicamentos por 7 dias seguidos.", R.drawable.ic_military_tech, 7),

            // Registros
            Achievement(AchievementId.LOG_10_DOSES, "Registrador Junior", "Registre um total de 10 doses.", R.drawable.ic_pill, 10),
            Achievement(AchievementId.LOG_50_DOSES, "Registrador Pleno", "Registre um total de 50 doses.", R.drawable.ic_pill, 50),
            Achievement(AchievementId.LOG_100_DOSES, "Registrador Sênior", "Registre um total de 100 doses.", R.drawable.ic_pill, 100),
            Achievement(AchievementId.LOG_10_NOTES, "Anotador Diligente", "Faça 10 anotações de saúde.", R.drawable.ic_notes, 10),
            Achievement(AchievementId.LOG_5_WEIGHTS, "Na Balança", "Registre seu peso 5 vezes.", R.drawable.ic_weight, 5),

            // Bem-Estar
            Achievement(AchievementId.HYDRATION_GOAL_7_DAYS, "Bem Hidratado", "Atinja sua meta de hidratação por 7 dias.", R.drawable.ic_water_drop, 7),
            Achievement(AchievementId.ACTIVITY_GOAL_7_DAYS, "Em Movimento", "Atinja sua meta de atividade física por 7 dias.", R.drawable.ic_fitness, 7),
            Achievement(AchievementId.LOG_ALL_WELLBEING_IN_ONE_DAY, "Dia Completo", "Registre hidratação, atividade, refeição e sono no mesmo dia.", R.drawable.ic_wellbeing, 4),

            // IA e Ferramentas
            Achievement(AchievementId.USE_AI_CHAT, "Conversa Inteligente", "Faça sua primeira pergunta ao Assistente IA.", R.drawable.ic_chat, 1),
            Achievement(AchievementId.USE_PREDICTIVE_ANALYSIS, "Olhando para o Futuro", "Gere sua primeira Análise Preditiva.", R.drawable.ic_ai_analysis, 1),
            Achievement(AchievementId.USE_RECIPE_SCANNER, "Adeus, Digitação!", "Escaneie sua primeira receita médica.", R.drawable.ic_scan, 1),
            Achievement(AchievementId.USE_MEAL_ANALYSIS, "Nutricionista de Bolso", "Analise sua primeira refeição com a câmera.", R.drawable.ic_food, 1),
            Achievement(AchievementId.CREATE_FIRST_REPORT, "Pronto para a Consulta", "Gere seu primeiro relatório em PDF.", R.drawable.ic_description, 1),

            // Social
            Achievement(AchievementId.INVITE_CAREGIVER, "Time Formado", "Convide outro cuidador para um Círculo de Cuidado.", R.drawable.ic_group_add, 1)
        )
    }
}