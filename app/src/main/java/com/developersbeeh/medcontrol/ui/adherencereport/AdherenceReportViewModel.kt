package com.developersbeeh.medcontrol.ui.adherencereport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.roundToInt

// Enum para o seletor de período (7, 30, 90 dias)
enum class ReportPeriod(val days: Long) {
    DAYS_7(7),
    DAYS_30(30),
    DAYS_90(90)
}

// Data classes para armazenar os dados calculados que a UI irá exibir
data class AdherenceByMedication(
    val medicationName: String,
    val adherencePercentage: Int
)

data class AdherenceByTimeOfDay(
    val periodName: String, // "Manhã", "Tarde", "Noite"
    val adherencePercentage: Int
)

data class AdherenceReportData(
    val overallAdherence: Int,
    val totalDosesTaken: Int,
    val totalDosesExpected: Int,
    val byMedication: List<AdherenceByMedication>,
    val byTimeOfDay: List<AdherenceByTimeOfDay>
)

@HiltViewModel
class AdherenceReportViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private lateinit var dependentId: String
    private var allMedications: List<Medicamento> = emptyList()
    private var allDoseHistory: List<com.developersbeeh.medcontrol.data.model.DoseHistory> = emptyList()

    private val _reportData = MutableLiveData<UiState<AdherenceReportData>>()
    val reportData: LiveData<UiState<AdherenceReportData>> = _reportData

    fun initialize(id: String) {
        if (this::dependentId.isInitialized && dependentId == id) return
        dependentId = id
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _reportData.postValue(UiState.Loading)
            try {
                // Carrega todos os dados uma única vez
                allMedications = medicationRepository.getMedicamentos(dependentId).first()
                allDoseHistory = medicationRepository.getDoseHistory(dependentId).first()
                // Gera o relatório para o período padrão (últimos 30 dias)
                generateReport(ReportPeriod.DAYS_30)
            } catch (e: Exception) {
                _reportData.postValue(UiState.Error("Falha ao carregar dados do histórico."))
            }
        }
    }

    fun generateReport(period: ReportPeriod) {
        viewModelScope.launch {
            _reportData.postValue(UiState.Loading)

            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(period.days - 1)

            val scheduledMeds = allMedications.filter { !it.isUsoEsporadico && it.horarios.isNotEmpty() }
            val dosesInPeriod = allDoseHistory.filter { it.timestamp.toLocalDate() in startDate..endDate }

            if (scheduledMeds.isEmpty()) {
                _reportData.postValue(UiState.Success(AdherenceReportData(0, 0, 0, emptyList(), emptyList())))
                return@launch
            }

            // 1. Cálculo Geral
            val totalExpected = DoseTimeCalculator.calculateExpectedDosesForPeriod(scheduledMeds, startDate, endDate)
            val totalTaken = dosesInPeriod.size
            val overallAdherence = if (totalExpected > 0) ((totalTaken.toDouble() / totalExpected) * 100).roundToInt().coerceAtMost(100) else 0

            // 2. Cálculo por Medicamento
            val byMedication = scheduledMeds.map { med ->
                val expected = DoseTimeCalculator.calculateExpectedDosesForMedicationInRange(med, startDate, endDate)
                val taken = dosesInPeriod.count { it.medicamentoId == med.id }
                val adherence = if (expected > 0) ((taken.toDouble() / expected) * 100).roundToInt().coerceAtMost(100) else 0
                AdherenceByMedication(med.nome, adherence)
            }.sortedBy { it.adherencePercentage }

            // 3. Cálculo por Período do Dia
            val byTimeOfDay = calculateAdherenceByTimeOfDay(scheduledMeds, dosesInPeriod, startDate, endDate)

            _reportData.postValue(UiState.Success(
                AdherenceReportData(
                    overallAdherence = overallAdherence,
                    totalDosesTaken = totalTaken,
                    totalDosesExpected = totalExpected,
                    byMedication = byMedication,
                    byTimeOfDay = byTimeOfDay
                )
            ))
        }
    }

    private fun calculateAdherenceByTimeOfDay(meds: List<Medicamento>, doses: List<DoseHistory>, start: LocalDate, end: LocalDate): List<AdherenceByTimeOfDay> {
        val morningRange = LocalTime.of(5, 0)..LocalTime.of(11, 59)
        val afternoonRange = LocalTime.of(12, 0)..LocalTime.of(17, 59)
        val nightRange1 = LocalTime.of(18, 0)..LocalTime.MAX
        val nightRange2 = LocalTime.MIN..LocalTime.of(4, 59)

        var expectedMorning = 0
        var expectedAfternoon = 0
        var expectedNight = 0

        var takenMorning = 0
        var takenAfternoon = 0
        var takenNight = 0

        // Calcula doses esperadas por período
        meds.forEach { med ->
            var currentDate = start
            while (!currentDate.isAfter(end)) {
                if (DoseTimeCalculator.isMedicationDay(med, currentDate)) {
                    med.horarios.forEach { horario ->
                        when (horario) {
                            in morningRange -> expectedMorning++
                            in afternoonRange -> expectedAfternoon++
                            in nightRange1, in nightRange2 -> expectedNight++
                        }
                    }
                }
                currentDate = currentDate.plusDays(1)
            }
        }

        // Conta doses tomadas por período
        doses.forEach { dose ->
            when (dose.timestamp.toLocalTime()) {
                in morningRange -> takenMorning++
                in afternoonRange -> takenAfternoon++
                in nightRange1, in nightRange2 -> takenNight++
            }
        }

        val morningAdherence = if (expectedMorning > 0) ((takenMorning.toDouble() / expectedMorning) * 100).roundToInt().coerceAtMost(100) else 0
        val afternoonAdherence = if (expectedAfternoon > 0) ((takenAfternoon.toDouble() / expectedAfternoon) * 100).roundToInt().coerceAtMost(100) else 0
        val nightAdherence = if (expectedNight > 0) ((takenNight.toDouble() / expectedNight) * 100).roundToInt().coerceAtMost(100) else 0

        return listOf(
            AdherenceByTimeOfDay("Manhã", morningAdherence),
            AdherenceByTimeOfDay("Tarde", afternoonAdherence),
            AdherenceByTimeOfDay("Noite", nightAdherence)
        )
    }
}