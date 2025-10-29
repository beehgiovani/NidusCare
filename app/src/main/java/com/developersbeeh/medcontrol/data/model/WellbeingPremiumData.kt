package com.developersbeeh.medcontrol.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ✅ NOVO: Modelos de dados premium para Diário do Bem-Estar
 * Todos os campos são nullable para compatibilidade
 */

/**
 * Análise de humor e bem-estar ao longo do tempo
 */
data class MoodAnalysis(
    val period: String, // "7days", "30days", "90days"
    val averageMood: Double, // 1.0 a 5.0
    val moodTrend: MoodTrend,
    val dominantMood: String,
    val moodDistribution: Map<String, Int>,
    val insights: List<MoodInsight>,
    val correlations: List<MoodCorrelation>? = null
)

enum class MoodTrend {
    IMPROVING,
    STABLE,
    DECLINING,
    FLUCTUATING
}

data class MoodInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val recommendation: String? = null,
    val icon: String? = null
)

/**
 * Correlações entre humor e outros fatores
 */
data class MoodCorrelation(
    val factor: String, // "sleep", "exercise", "medication_adherence"
    val correlation: Double, // -1.0 a 1.0
    val strength: CorrelationStrength,
    val description: String
)

enum class CorrelationStrength {
    WEAK,
    MODERATE,
    STRONG,
    VERY_STRONG
}

/**
 * Análise de padrões de sono
 */
data class SleepAnalysis(
    val averageHours: Double,
    val averageQuality: Double, // 1.0 a 5.0
    val sleepTrend: SleepTrend,
    val bestSleepDay: String? = null,
    val worstSleepDay: String? = null,
    val insights: List<SleepInsight>,
    val recommendations: List<String>
)

enum class SleepTrend {
    IMPROVING,
    STABLE,
    WORSENING
}

data class SleepInsight(
    val type: String,
    val message: String,
    val severity: String // "info", "warning", "critical"
)

/**
 * Análise de atividade física
 */
data class ActivityAnalysis(
    val totalMinutes: Int,
    val averageMinutesPerDay: Double,
    val mostFrequentActivity: String? = null,
    val activityDistribution: Map<String, Int>,
    val weeklyGoalProgress: Double, // 0.0 a 1.0
    val insights: List<ActivityInsight>,
    val recommendations: List<String>
)

data class ActivityInsight(
    val type: String,
    val message: String,
    val icon: String? = null
)

/**
 * Análise nutricional avançada
 */
data class NutritionAnalysis(
    val totalMeals: Int,
    val averageMealsPerDay: Double,
    val mealDistribution: Map<String, Int>, // breakfast, lunch, dinner, snack
    val nutritionScore: Double, // 0.0 a 10.0
    val insights: List<NutritionInsight>,
    val recommendations: List<String>,
    val topFoods: List<String>? = null
)

data class NutritionInsight(
    val type: String,
    val message: String,
    val severity: String
)

/**
 * Análise de hidratação
 */
data class HydrationAnalysis(
    val averageWaterIntake: Double, // em mL
    val dailyGoal: Double,
    val goalAchievementRate: Double, // 0.0 a 1.0
    val trend: HydrationTrend,
    val bestDay: LocalDate? = null,
    val insights: List<String>,
    val recommendations: List<String>
)

enum class HydrationTrend {
    IMPROVING,
    STABLE,
    DECLINING
}

/**
 * Relatório de bem-estar completo
 */
data class WellbeingReport(
    val dependentId: String,
    val dependentName: String,
    val period: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val overallScore: Double, // 0.0 a 10.0
    val moodAnalysis: MoodAnalysis,
    val sleepAnalysis: SleepAnalysis,
    val activityAnalysis: ActivityAnalysis,
    val nutritionAnalysis: NutritionAnalysis,
    val hydrationAnalysis: HydrationAnalysis,
    val generalInsights: List<GeneralInsight>,
    val actionPlan: List<ActionItem>,
    val generatedAt: String
)

data class GeneralInsight(
    val category: String,
    val title: String,
    val description: String,
    val priority: InsightPriority
)

enum class InsightPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

data class ActionItem(
    val title: String,
    val description: String,
    val category: String,
    val priority: Int,
    val estimatedImpact: String // "low", "medium", "high"
)

/**
 * Objetivo de bem-estar (Premium)
 */
data class WellbeingGoal(
    val goalId: String? = null,
    val userId: String,
    val dependentId: String,
    val type: GoalType,
    val title: String,
    val description: String,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String, // "hours", "minutes", "ml", "times"
    val frequency: GoalFrequency,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = false,
    val progress: Double = 0.0, // 0.0 a 1.0
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class GoalType {
    SLEEP,
    EXERCISE,
    WATER,
    MEDITATION,
    MOOD,
    CUSTOM
}

enum class GoalFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

/**
 * Conquista de bem-estar (Gamification)
 */
data class WellbeingAchievement(
    val achievementId: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String,
    val points: Int,
    val unlockedAt: String? = null,
    val isUnlocked: Boolean = false,
    val progress: Double = 0.0, // 0.0 a 1.0
    val requirement: String
)

/**
 * Streak (sequência de dias consecutivos)
 */
data class WellbeingStreak(
    val type: String, // "daily_log", "exercise", "sleep_goal"
    val currentStreak: Int,
    val longestStreak: Int,
    val lastLogDate: LocalDate? = null
)

/**
 * Comparação de períodos
 */


data class PeriodData(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val value: Double
)

/**
 * Previsão de bem-estar (IA)
 */
data class WellbeingPrediction(
    val predictedMood: Double,
    val predictedSleepQuality: Double,
    val predictedEnergyLevel: Double,
    val confidence: Double, // 0.0 a 1.0
    val basedOn: List<String>, // fatores considerados
    val recommendations: List<String>,
    val predictionDate: LocalDate
)

/**
 * Alerta de bem-estar
 */
data class WellbeingAlert(
    val alertId: String? = null,
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val recommendation: String? = null,
    val createdAt: String,
    val isRead: Boolean = false
)

enum class AlertType {
    MOOD_DECLINE,
    SLEEP_ISSUE,
    LOW_ACTIVITY,
    DEHYDRATION,
    MISSED_GOAL,
    STREAK_BROKEN
}

/**
 * Entrada de diário enriquecida
 */
data class EnhancedDiaryEntry(
    val entryId: String? = null,
    val dependentId: String,
    val date: LocalDate,
    val mood: String,
    val moodScore: Int, // 1-5
    val energyLevel: Int? = null, // 1-5
    val stressLevel: Int? = null, // 1-5
    val notes: String? = null,
    val tags: List<String>? = null,
    val weather: String? = null,
    val location: String? = null,
    val activities: List<String>? = null,
    val symptoms: List<String>? = null,
    val triggers: List<String>? = null,
    val gratitude: List<String>? = null,
    val photoUrls: List<String>? = null,
    val voiceNoteUrl: String? = null,
    val aiSummary: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Template de diário (Premium)
 */
data class DiaryTemplate(
    val templateId: String,
    val name: String,
    val description: String,
    val prompts: List<DiaryPrompt>,
    val isDefault: Boolean = false,
    val isPremium: Boolean = false
)

data class DiaryPrompt(
    val question: String,
    val type: PromptType,
    val isRequired: Boolean = false
)

enum class PromptType {
    TEXT,
    SCALE,
    MULTIPLE_CHOICE,
    TAGS,
    PHOTO
}

