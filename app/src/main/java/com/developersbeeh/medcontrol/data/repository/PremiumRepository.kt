package com.developersbeeh.medcontrol.data.repository

import com.developersbeeh.medcontrol.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ NOVO: Repositório para funcionalidades premium
 * Gerencia chamadas para Cloud Functions premium e dados locais
 */
@Singleton
class PremiumRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) {

    /**
     * Calcula custos de medicamentos para um período
     */
    suspend fun calculateMedicationCosts(
        dependentId: String,
        startDate: String,
        endDate: String
    ): Result<FinancialReport> {
        return try {
            val data = hashMapOf(
                "dependentId" to dependentId,
                "startDate" to startDate,
                "endDate" to endDate
            )

            val result = functions
                .getHttpsCallable("calculateMedicationCosts")
                .call(data)
                .await()

            val response = result.data as? Map<*, *>
            if (response?.get("success") == true) {
                val reportData = response["data"] as? Map<*, *>
                val report = parseFinancialReport(reportData)
                Result.success(report)
            } else {
                Result.failure(Exception("Erro ao calcular custos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analisa interações medicamentosas
     */
    suspend fun analyzeMedicationInteractions(
        dependentId: String
    ): Result<InteractionReport> {
        return try {
            val data = hashMapOf("dependentId" to dependentId)

            val result = functions
                .getHttpsCallable("analyzeMedicationInteractions")
                .call(data)
                .await()

            val response = result.data as? Map<*, *>
            if (response?.get("success") == true) {
                val reportData = response["data"] as? Map<*, *>
                val report = parseInteractionReport(reportData)
                Result.success(report)
            } else {
                Result.failure(Exception("Erro ao analisar interações"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Prevê padrões de aderência
     */
    suspend fun predictAdherencePatterns(
        dependentId: String,
        daysToPredict: Int = 30
    ): Result<AdherencePrediction> {
        return try {
            val data = hashMapOf(
                "dependentId" to dependentId,
                "daysToPredict" to daysToPredict
            )

            val result = functions
                .getHttpsCallable("predictAdherencePatterns")
                .call(data)
                .await()

            val response = result.data as? Map<*, *>
            if (response?.get("success") == true) {
                val reportData = response["data"] as? Map<*, *>
                val prediction = parseAdherencePrediction(reportData)
                Result.success(prediction)
            } else {
                Result.failure(Exception("Erro ao prever aderência"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gera relatório comparativo entre dependentes
     */
    suspend fun generateComparativeReport(
        dependentIds: List<String>,
        startDate: String,
        endDate: String
    ): Result<ComparativeReport> {
        return try {
            val data = hashMapOf(
                "dependentIds" to dependentIds,
                "startDate" to startDate,
                "endDate" to endDate
            )

            val result = functions
                .getHttpsCallable("generateComparativeReport")
                .call(data)
                .await()

            val response = result.data as? Map<*, *>
            if (response?.get("success") == true) {
                val reportData = response["data"] as? Map<*, *>
                val report = parseComparativeReport(reportData)
                Result.success(report)
            } else {
                Result.failure(Exception("Erro ao gerar relatório comparativo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calcula distâncias para farmácias
     */
    suspend fun calculatePharmacyDistances(
        userLat: Double,
        userLng: Double,
        pharmacies: List<Map<String, Any>>
    ): Result<List<Map<String, Any>>> {
        return try {
            val data = hashMapOf(
                "userLat" to userLat,
                "userLng" to userLng,
                "pharmacies" to pharmacies
            )

            val result = functions
                .getHttpsCallable("calculatePharmacyDistances")
                .call(data)
                .await()

            val response = result.data as? Map<*, *>
            if (response?.get("success") == true) {
                @Suppress("UNCHECKED_CAST")
                val pharmaciesWithDistance = response["data"] as? List<Map<String, Any>>
                Result.success(pharmaciesWithDistance ?: emptyList())
            } else {
                Result.failure(Exception("Erro ao calcular distâncias"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Salva farmácia favorita
     */
    suspend fun saveFavoritePharmacy(
        pharmacyId: String,
        pharmacyName: String,
        pharmacyAddress: String?,
        pharmacyPhone: String?
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Usuário não autenticado")
            
            val data = hashMapOf(
                "pharmacyId" to pharmacyId,
                "pharmacyName" to pharmacyName,
                "pharmacyAddress" to pharmacyAddress,
                "pharmacyPhone" to pharmacyPhone
            )

            functions
                .getHttpsCallable("saveFavoritePharmacy")
                .call(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Agenda envio automático de relatório
     */
    suspend fun scheduleReport(
        dependentId: String,
        email: String,
        reportType: ReportType,
        frequency: ReportFrequency
    ): Result<String> {
        return try {
            val data = hashMapOf(
                "dependentId" to dependentId,
                "email" to email,
                "reportType" to reportType.name,
                "frequency" to frequency.name
            )

            val result = functions
                .getHttpsCallable("sendScheduledReport")
                .call(data)
                .await()

            val response = result.data as? Map<*, *>
            if (response?.get("success") == true) {
                val scheduleId = response["scheduleId"] as? String
                Result.success(scheduleId ?: "")
            } else {
                Result.failure(Exception("Erro ao agendar relatório"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // Funções auxiliares de parsing
    // ========================================

    private fun parseFinancialReport(data: Map<*, *>?): FinancialReport {
        if (data == null) return FinancialReport()
        
        @Suppress("UNCHECKED_CAST")
        val breakdown = (data["medicationBreakdown"] as? List<Map<*, *>>)?.map { med ->
            MedicationCost(
                medicationId = med["medicationId"] as? String ?: "",
                medicationName = med["medicationName"] as? String ?: "",
                expectedDoses = (med["expectedDoses"] as? Number)?.toInt() ?: 0,
                costPerDose = (med["costPerDose"] as? Number)?.toDouble() ?: 0.0,
                totalCost = (med["totalCost"] as? Number)?.toDouble() ?: 0.0
            )
        } ?: emptyList()

        return FinancialReport(
            totalCost = (data["totalCost"] as? Number)?.toDouble() ?: 0.0,
            avgCostPerDay = (data["avgCostPerDay"] as? Number)?.toDouble() ?: 0.0,
            periodDays = (data["periodDays"] as? Number)?.toInt() ?: 0,
            projectedMonthlyCost = (data["projectedMonthlyCost"] as? Number)?.toDouble() ?: 0.0,
            projectedYearlyCost = (data["projectedYearlyCost"] as? Number)?.toDouble() ?: 0.0,
            medicationBreakdown = breakdown
        )
    }

    private fun parseInteractionReport(data: Map<*, *>?): InteractionReport {
        if (data == null) return InteractionReport(0, 0, emptyList(), "", "")
        
        @Suppress("UNCHECKED_CAST")
        val interactions = (data["interactions"] as? List<Map<*, *>>)?.map { interaction ->
            MedicationInteraction(
                medication1 = interaction["medication1"] as? String ?: "",
                medication2 = interaction["medication2"] as? String ?: "",
                severity = interaction["severity"] as? String ?: "",
                description = interaction["description"] as? String ?: "",
                recommendation = interaction["recommendation"] as? String ?: ""
            )
        } ?: emptyList()

        return InteractionReport(
            totalMedications = (data["totalMedications"] as? Number)?.toInt() ?: 0,
            interactionsFound = (data["interactionsFound"] as? Number)?.toInt() ?: 0,
            interactions = interactions,
            analysisDate = data["analysisDate"] as? String ?: "",
            disclaimer = data["disclaimer"] as? String ?: ""
        )
    }

    private fun parseAdherencePrediction(data: Map<*, *>?): AdherencePrediction {
        if (data == null) {
            return AdherencePrediction(
                HistoricalAdherence(0.0, 0, 0, 0, 0),
                emptyList(),
                FutureAdherence(0, 0, 0, 0, 0.0),
                emptyList(),
                ""
            )
        }

        val historical = data["historicalAdherence"] as? Map<*, *>
        val historicalAdherence = HistoricalAdherence(
            rate = (historical?.get("rate") as? Number)?.toDouble() ?: 0.0,
            totalDoses = (historical?.get("totalDoses") as? Number)?.toInt() ?: 0,
            taken = (historical?.get("taken") as? Number)?.toInt() ?: 0,
            missed = (historical?.get("missed") as? Number)?.toInt() ?: 0,
            periodDays = (historical?.get("periodDays") as? Number)?.toInt() ?: 0
        )

        @Suppress("UNCHECKED_CAST")
        val weekdayList = (data["adherenceByWeekday"] as? List<Map<*, *>>)?.map { day ->
            WeekdayAdherence(
                weekday = (day["weekday"] as? Number)?.toInt() ?: 0,
                weekdayName = day["weekdayName"] as? String ?: "",
                taken = (day["taken"] as? Number)?.toInt() ?: 0,
                missed = (day["missed"] as? Number)?.toInt() ?: 0,
                rate = (day["rate"] as? Number)?.toDouble() ?: 0.0
            )
        } ?: emptyList()

        val pred = data["prediction"] as? Map<*, *>
        val prediction = FutureAdherence(
            daysAhead = (pred?.get("daysAhead") as? Number)?.toInt() ?: 0,
            expectedDoses = (pred?.get("expectedDoses") as? Number)?.toInt() ?: 0,
            predictedTaken = (pred?.get("predictedTaken") as? Number)?.toInt() ?: 0,
            predictedMissed = (pred?.get("predictedMissed") as? Number)?.toInt() ?: 0,
            predictedRate = (pred?.get("predictedRate") as? Number)?.toDouble() ?: 0.0
        )

        @Suppress("UNCHECKED_CAST")
        val insightsList = (data["insights"] as? List<Map<*, *>>)?.map { insight ->
            AdherenceInsight(
                type = insight["type"] as? String ?: "",
                message = insight["message"] as? String ?: "",
                icon = insight["icon"] as? String ?: ""
            )
        } ?: emptyList()

        return AdherencePrediction(
            historicalAdherence = historicalAdherence,
            adherenceByWeekday = weekdayList,
            prediction = prediction,
            insights = insightsList,
            analysisDate = data["analysisDate"] as? String ?: ""
        )
    }

    private fun parseComparativeReport(data: Map<*, *>?): ComparativeReport {
        if (data == null) return ComparativeReport()

        @Suppress("UNCHECKED_CAST")
        val comparisons = (data["comparisons"] as? List<Map<*, *>>)?.map { comp ->
            DependentComparison(
                dependentId = comp["dependentId"] as? String ?: "",
                dependentName = comp["dependentName"] as? String ?: "",
                age = (comp["age"] as? Number)?.toInt(),
                totalMedications = (comp["totalMedications"] as? Number)?.toInt() ?: 0,
                expectedDoses = (comp["expectedDoses"] as? Number)?.toInt() ?: 0,
                takenDoses = (comp["takenDoses"] as? Number)?.toInt() ?: 0,
                adherenceRate = (comp["adherenceRate"] as? Number)?.toDouble() ?: 0.0
            )
        } ?: emptyList()

        val summaryData = data["summary"] as? Map<*, *>
        val summary = if (summaryData != null && comparisons.isNotEmpty()) {
            ComparativeSummary(
                bestAdherence = comparisons.first(),
                worstAdherence = comparisons.last(),
                averageAdherence = (summaryData["averageAdherence"] as? Number)?.toDouble() ?: 0.0
            )
        } else null

        return ComparativeReport(
            comparisons = comparisons,
            summary = summary
        )
    }
}

