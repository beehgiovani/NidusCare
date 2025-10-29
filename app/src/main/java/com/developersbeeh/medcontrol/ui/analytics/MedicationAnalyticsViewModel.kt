package com.developersbeeh.medcontrol.ui.analytics

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.github.mikephil.charting.data.Entry
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoDosagem
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject

private const val TAG = "MedicationAnalyticsVM"

data class DailyAdherenceSummary(
    val dosesTaken: Int,
    val dosesExpected: Int
)

data class SporadicStat(val name: String, val timesTaken: Int)

data class VariableDoseStat(
    val nome: String,
    val doseMedia: Double,
    val doseMin: Double,
    val doseMax: Double,
    val unidade: String
)

data class WeeklyChartData(
    val entries: List<BarEntryData>,
    val labels: List<LocalDate>
)

data class BarEntryData(
    val index: Float,
    val adherencePercentage: Float,
    val isOverdosed: Boolean
)

data class AnalyticsData(
    val dailySummary: DailyAdherenceSummary?,
    val weeklyData: WeeklyChartData,
    val monthlyData: List<Entry>,
    val monthlyLabels: List<LocalDate>,
    val sporadicStats: List<SporadicStat>,
    val variableDoseStats: List<VariableDoseStat>
)

@HiltViewModel
class MedicationAnalyticsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _dependentId = MutableLiveData<String>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val analyticsDataFlow = _dependentId.asFlow().flatMapLatest { id ->
        if (id.isBlank()) {
            return@flatMapLatest flowOf(
                AnalyticsData(
                    null,
                    WeeklyChartData(emptyList(), emptyList()),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList()
                )
            )
        }
        val medsFlow = medicationRepository.getMedicamentos(id)
        val historyFlow = medicationRepository.getDoseHistory(id)

        combine(medsFlow, historyFlow) { meds, history ->
            calculateAnalytics(history, meds)
        }
    }

    val analyticsData: LiveData<AnalyticsData> = analyticsDataFlow.asLiveData()

    fun initialize(id: String) {
        if (_dependentId.value != id) {
            _dependentId.value = id
        }
    }

    private fun calculateAnalytics(
        history: List<DoseHistory>,
        medications: List<Medicamento>
    ): AnalyticsData {
        Log.d(TAG, "calculateAnalytics: Início da análise para ${medications.size} medicamentos e ${history.size} doses.")
        val today = LocalDate.now()
        val sevenDaysAgo = today.minusDays(6)
        val thirtyDaysAgo = today.minusDays(29)

        val sporadicMeds = medications.filter { it.isUsoEsporadico }
        val recentHistory = history.filter {
            val doseDate = it.timestamp.toLocalDate()
            !doseDate.isBefore(sevenDaysAgo) && !doseDate.isAfter(today)
        }
        val sporadicStatsList = sporadicMeds.map { med ->
            val count = recentHistory.count { it.medicamentoId == med.id }
            SporadicStat(med.nome, count)
        }.filter { it.timesTaken > 0 }

        val variableDoseMeds = medications.filter { it.tipoDosagem == TipoDosagem.MANUAL || it.tipoDosagem == TipoDosagem.CALCULADA }
        val monthlyHistory = history.filter {
            val doseDate = it.timestamp.toLocalDate()
            !doseDate.isBefore(thirtyDaysAgo) && !doseDate.isAfter(today)
        }
        val variableDoseStatsList = variableDoseMeds.mapNotNull { med ->
            val dosesRelevantes = monthlyHistory.filter { it.medicamentoId == med.id && it.quantidadeAdministrada != null }
            if (dosesRelevantes.isNotEmpty()) {
                val quantidades = dosesRelevantes.map { it.quantidadeAdministrada!! }
                VariableDoseStat(
                    nome = med.nome,
                    doseMedia = quantidades.average(),
                    doseMin = quantidades.minOrNull() ?: 0.0,
                    doseMax = quantidades.maxOrNull() ?: 0.0,
                    unidade = med.unidadeDeEstoque
                )
            } else {
                null
            }
        }

        val scheduledMedications = medications.filter { !it.isUsoEsporadico }
        val existingMedIds = scheduledMedications.map { it.id }.toSet()
        val validHistory = history.filter { existingMedIds.contains(it.medicamentoId) }

        val dosesExpectedToday = DoseTimeCalculator.calculateExpectedDosesForPeriod(scheduledMedications, today, today)
        val dosesTakenToday = validHistory.count { it.timestamp.toLocalDate() == today }
        val dailyAdherenceSummary = DailyAdherenceSummary(dosesTakenToday, dosesExpectedToday)

        val weeklyStartDate = today.minusDays(6)
        val weeklyDates = (0..6).map { weeklyStartDate.plusDays(it.toLong()) }

        val weeklyEntriesData = weeklyDates.mapIndexed { index, date ->
            val expectedOnDay = DoseTimeCalculator.calculateExpectedDosesForPeriod(scheduledMedications, date, date)
            val takenOnDay = validHistory.count { it.timestamp.toLocalDate() == date }

            val adherenceOnDay: Float
            val isOverdosedOnDay: Boolean

            if (expectedOnDay > 0) {
                adherenceOnDay = (takenOnDay.toFloat() / expectedOnDay * 100)
                isOverdosedOnDay = takenOnDay > expectedOnDay
            } else {
                isOverdosedOnDay = takenOnDay > 0
                adherenceOnDay = if (isOverdosedOnDay) 100f else 0f
            }

            BarEntryData(
                index = index.toFloat(),
                adherencePercentage = adherenceOnDay,
                isOverdosed = isOverdosedOnDay
            )
        }
        val weeklyData = WeeklyChartData(weeklyEntriesData, weeklyDates)

        val monthlyStartDate = today.minusDays(29)
        val monthlyDates = (0..29).map { monthlyStartDate.plusDays(it.toLong()) }
        val monthlyEntries = monthlyDates.mapIndexed { index, date ->
            val expectedOnDay = DoseTimeCalculator.calculateExpectedDosesForPeriod(scheduledMedications, date, date)
            val takenOnDay = validHistory.count { it.timestamp.toLocalDate() == date }

            // --- INÍCIO DA CORREÇÃO ---
            val adherenceOnDay = if (expectedOnDay > 0) {
                (takenOnDay.toFloat() / expectedOnDay * 100).coerceAtMost(100f)
            } else {
                // Se não havia doses esperadas, mas alguma foi tomada, exibe 100% para sinalizar atividade.
                if (takenOnDay > 0) 100f else 0f
            }
            // --- FIM DA CORREÇÃO ---

            Entry(index.toFloat(), adherenceOnDay)
        }

        Log.d(TAG, "calculateAnalytics: Análise concluída.")

        return AnalyticsData(
            dailySummary = dailyAdherenceSummary,
            weeklyData = weeklyData,
            monthlyData = monthlyEntries,
            monthlyLabels = monthlyDates,
            sporadicStats = sporadicStatsList,
            variableDoseStats = variableDoseStatsList
        )
    }
}