// src/main/java/com/developersbeeh/medcontrol/ui/analytics/HealthChartsViewModel.kt

package com.developersbeeh.medcontrol.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

enum class ChartType {
    BLOOD_PRESSURE,
    BLOOD_SUGAR,
    WEIGHT,
    TEMPERATURE
}

data class ChartData(
    val line1: List<Entry>? = null,
    val line2: List<Entry>? = null,
    val dates: List<LocalDate>
)

@HiltViewModel
class HealthChartsViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private lateinit var dependentId: String
    private var allHealthNotes: List<HealthNote> = emptyList()
    private var currentDependent: Dependente? = null

    private val _chartData = MutableLiveData<ChartData?>()
    val chartData: LiveData<ChartData?> = _chartData

    private var selectedChartType = ChartType.BLOOD_PRESSURE
    private var selectedPeriodDays: Long = 30 // Padrão para 30 dias

    fun initialize(id: String) {
        if (this::dependentId.isInitialized && dependentId == id) return
        dependentId = id
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            allHealthNotes = repository.getHealthNotes(dependentId).first()
            currentDependent = repository.getDependente(dependentId)
            processDataForChart()
        }
    }

    fun setPeriod(days: Long) {
        selectedPeriodDays = days
        processDataForChart()
    }

    fun selectChartType(type: ChartType) {
        selectedChartType = type
        processDataForChart()
    }

    private fun processDataForChart() {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(selectedPeriodDays)

        val filteredNotesByDate = allHealthNotes
            .filter {
                val noteDate = it.timestamp.toLocalDate()
                !noteDate.isBefore(startDate) && !noteDate.isAfter(endDate)
            }

        val filteredNotesByType = filteredNotesByDate
            .filter { it.type.name == selectedChartType.name }
            .sortedBy { it.timestamp }

        if (filteredNotesByType.isEmpty() && selectedChartType != ChartType.WEIGHT) {
            _chartData.postValue(null)
            return
        }

        val dates = filteredNotesByType.map { it.timestamp.toLocalDate() }
        var line1: List<Entry>? = null
        var line2: List<Entry>? = null

        when (selectedChartType) {
            ChartType.BLOOD_PRESSURE -> {
                line1 = filteredNotesByType.map { note ->
                    Entry(
                        note.timestamp.toEpochSecond(ZoneOffset.UTC).toFloat(),
                        note.values["systolic"]?.toFloatOrNull() ?: 0f
                    )
                }
                line2 = filteredNotesByType.map { note ->
                    Entry(
                        note.timestamp.toEpochSecond(ZoneOffset.UTC).toFloat(),
                        note.values["diastolic"]?.toFloatOrNull() ?: 0f
                    )
                }
            }
            ChartType.BLOOD_SUGAR -> {
                line1 = filteredNotesByType.map { note ->
                    Entry(
                        note.timestamp.toEpochSecond(ZoneOffset.UTC).toFloat(),
                        note.values["sugarLevel"]?.toFloatOrNull() ?: 0f
                    )
                }
            }
            ChartType.WEIGHT -> {
                line1 = filteredNotesByType.map { note ->
                    Entry(
                        note.timestamp.toEpochSecond(ZoneOffset.UTC).toFloat(),
                        note.values["weight"]?.toFloatOrNull() ?: 0f
                    )
                }
                // Adiciona a linha da meta de peso
                val targetWeight = currentDependent?.pesoMeta?.replace(',', '.')?.toFloatOrNull()
                if (targetWeight != null && targetWeight > 0 && line1.isNotEmpty()) {
                    val firstTimestamp = line1.first().x
                    val lastTimestamp = line1.last().x
                    line2 = listOf(
                        Entry(firstTimestamp, targetWeight),
                        Entry(lastTimestamp, targetWeight)
                    )
                }
            }
            ChartType.TEMPERATURE -> {
                line1 = filteredNotesByType.map { note ->
                    Entry(
                        note.timestamp.toEpochSecond(ZoneOffset.UTC).toFloat(),
                        note.values["temperature"]?.replace(',','.')?.toFloatOrNull() ?: 0f
                    )
                }
            }
        }
        _chartData.postValue(ChartData(line1, line2, dates))
    }
}