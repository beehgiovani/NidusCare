package com.developersbeeh.medcontrol.data.model

import java.time.LocalDate

/**
 * ✅ NOVO: Data classes para relatórios premium avançados
 * Todos os campos são nullable para compatibilidade com data models existentes
 */

/**
 * Relatório Financeiro - Gastos com medicamentos e saúde
 */
data class FinancialReport(
    val totalCost: Double = 0.0,
    val avgCostPerDay: Double = 0.0,
    val periodDays: Int = 0,
    val projectedMonthlyCost: Double = 0.0,
    val projectedYearlyCost: Double = 0.0,
    val medicationBreakdown: List<MedicationCost> = emptyList(),
    val appointmentCosts: Double? = null,
    val examCosts: Double? = null
)

data class MedicationCost(
    val medicationId: String,
    val medicationName: String,
    val expectedDoses: Int,
    val costPerDose: Double,
    val totalCost: Double
)

/**
 * Relatório de Tendências - Análise de padrões ao longo do tempo
 */
data class TrendReport(
    val adherenceTrend: List<TrendPoint> = emptyList(),
    val healthMetricsTrend: Map<String, List<TrendPoint>> = emptyMap(),
    val insights: List<TrendInsight> = emptyList(),
    val periodComparison: PeriodComparison? = null
)

data class TrendPoint(
    val date: LocalDate,
    val value: Double,
    val label: String? = null
)

data class TrendInsight(
    val type: InsightType,
    val message: String,
    val severity: InsightSeverity,
    val icon: String? = null
)

enum class InsightType {
    IMPROVEMENT,
    DECLINE,
    STABLE,
    WARNING,
    SUCCESS
}

enum class InsightSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class PeriodComparison(
    val currentPeriod: PeriodStats,
    val previousPeriod: PeriodStats,
    val percentageChange: Double
)

data class PeriodStats(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val adherenceRate: Double,
    val totalDoses: Int,
    val missedDoses: Int
)

/**
 * Relatório Comparativo - Comparação entre dependentes
 */
data class ComparativeReport(
    val comparisons: List<DependentComparison> = emptyList(),
    val summary: ComparativeSummary? = null
)

data class DependentComparison(
    val dependentId: String,
    val dependentName: String,
    val age: Int? = null,
    val totalMedications: Int,
    val expectedDoses: Int,
    val takenDoses: Int,
    val adherenceRate: Double,
    val healthScore: Double? = null
)

data class ComparativeSummary(
    val bestAdherence: DependentComparison,
    val worstAdherence: DependentComparison,
    val averageAdherence: Double
)

/**
 * Relatório de Alertas - Medicamentos vencidos, consultas próximas, etc.
 */
data class AlertReport(
    val expiredMedications: List<MedicationAlert> = emptyList(),
    val upcomingAppointments: List<AppointmentAlert> = emptyList(),
    val lowStockMedications: List<StockAlert> = emptyList(),
    val missedDosesAlerts: List<MissedDoseAlert> = emptyList(),
    val interactionAlerts: List<InteractionAlert> = emptyList()
)

data class MedicationAlert(
    val medicationId: String,
    val medicationName: String,
    val expiryDate: LocalDate? = null,
    val daysUntilExpiry: Int? = null,
    val severity: AlertSeverity
)

data class AppointmentAlert(
    val appointmentId: String,
    val appointmentType: String,
    val appointmentDate: LocalDate,
    val daysUntilAppointment: Int,
    val doctorName: String? = null
)

data class StockAlert(
    val medicationId: String,
    val medicationName: String,
    val currentStock: Int,
    val daysRemaining: Int,
    val severity: AlertSeverity
)

data class MissedDoseAlert(
    val medicationId: String,
    val medicationName: String,
    val missedCount: Int,
    val lastMissedDate: LocalDate,
    val severity: AlertSeverity
)

data class InteractionAlert(
    val medication1: String,
    val medication2: String,
    val interactionType: String,
    val description: String,
    val severity: AlertSeverity,
    val recommendation: String? = null
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Relatório de Interações Medicamentosas
 */
data class InteractionReport(
    val totalMedications: Int,
    val interactionsFound: Int,
    val interactions: List<MedicationInteraction> = emptyList(),
    val analysisDate: String,
    val disclaimer: String
)

data class MedicationInteraction(
    val medication1: String,
    val medication2: String,
    val severity: String,
    val description: String,
    val recommendation: String
)

/**
 * Previsão de Aderência
 */
data class AdherencePrediction(
    val historicalAdherence: HistoricalAdherence,
    val adherenceByWeekday: List<WeekdayAdherence>,
    val prediction: FutureAdherence,
    val insights: List<AdherenceInsight>,
    val analysisDate: String
)

data class HistoricalAdherence(
    val rate: Double,
    val totalDoses: Int,
    val taken: Int,
    val missed: Int,
    val periodDays: Int
)

data class WeekdayAdherence(
    val weekday: Int,
    val weekdayName: String,
    val taken: Int,
    val missed: Int,
    val rate: Double
)

data class FutureAdherence(
    val daysAhead: Int,
    val expectedDoses: Int,
    val predictedTaken: Int,
    val predictedMissed: Int,
    val predictedRate: Double
)

data class AdherenceInsight(
    val type: String,
    val message: String,
    val icon: String
)

/**
 * Configuração de Relatório Agendado
 */
data class ScheduledReport(
    val scheduleId: String? = null,
    val userId: String,
    val dependentId: String,
    val email: String,
    val reportType: ReportType,
    val frequency: ReportFrequency,
    val createdAt: String? = null,
    val isActive: Boolean = true,
    val lastSent: String? = null
)

enum class ReportType {
    SIMPLE,
    DETAILED,
    FINANCIAL,
    TRENDS,
    COMPARATIVE,
    ALERTS,
    INTERACTIONS
}

enum class ReportFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

