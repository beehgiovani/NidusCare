// src/main/java/com/developersbeeh/medcontrol/ui/reports/ReportsViewModel.kt
package com.developersbeeh.medcontrol.ui.reports

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.repository.*
import com.developersbeeh.medcontrol.util.Event
import com.developersbeeh.medcontrol.util.PdfReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

data class ReportOptions(
    val includeDoseHistory: Boolean,
    val includeAppointments: Boolean,
    val includedNoteTypes: Set<HealthNoteType>,
    val includeAdherenceSummary: Boolean,
    val includeAdherenceChart: Boolean
)

sealed class ReportGenerationState {
    object Idle : ReportGenerationState()
    // O estado de Loading aqui não será mais usado para controlar a UI diretamente
    object Loading : ReportGenerationState()
    data class Success(val file: File) : ReportGenerationState()
    data class Error(val message: String) : ReportGenerationState()
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository,
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository,
    private val cycleRepository: CycleRepository,
    private val application: Application
) : ViewModel() {

    private lateinit var dependentId: String
    private lateinit var dependentName: String

    private val _reportState = MutableLiveData<Event<ReportGenerationState>>(Event(ReportGenerationState.Idle))
    val reportState: LiveData<Event<ReportGenerationState>> = _reportState

    // ✅ NOVOS EVENTOS PARA CONTROLAR O DIÁLOGO DE CARREGAMENTO
    private val _showLoading = MutableLiveData<Event<String>>()
    val showLoading: LiveData<Event<String>> = _showLoading

    private val _hideLoading = MutableLiveData<Event<Unit>>()
    val hideLoading: LiveData<Event<Unit>> = _hideLoading

    fun initialize(depId: String, depName: String) {
        dependentId = depId
        dependentName = depName
    }

    fun generateReport(
        startDate: LocalDate,
        endDate: LocalDate,
        options: ReportOptions,
        logoBitmap: Bitmap
    ) {
        // ✅ DISPARA O EVENTO PARA MOSTRAR O DIÁLOGO
        _showLoading.value = Event("Gerando relatório...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cuidadorId = userRepository.getCurrentUser()?.uid ?: throw Exception("Usuário não autenticado.")
                val isPremium = userRepository.isUserPremium(cuidadorId)
                val cuidadorResult = userRepository.getUserProfile(cuidadorId)
                val dependente = firestoreRepository.getDependente(dependentId) ?: throw Exception("Dependente não encontrado.")

                if (cuidadorResult.isFailure) {
                    throw Exception("Perfil do cuidador não encontrado.")
                }
                val cuidador = cuidadorResult.getOrThrow()

                val finalOptions = if (isPremium) options else ReportOptions(
                    includeDoseHistory = true,
                    includeAppointments = true,
                    includedNoteTypes = emptySet(),
                    includeAdherenceSummary = false,
                    includeAdherenceChart = false
                )

                val allMeds = if (finalOptions.includeAdherenceSummary || finalOptions.includeDoseHistory) medicationRepository.getMedicamentos(dependentId).first() else emptyList()
                val allDoses = if (finalOptions.includeDoseHistory || finalOptions.includeAdherenceSummary) medicationRepository.getDoseHistory(dependentId).first() else emptyList()
                val allNotes = if (finalOptions.includedNoteTypes.isNotEmpty()) firestoreRepository.getHealthNotes(dependentId).first() else emptyList()
                val allSchedules = if (finalOptions.includeAppointments) scheduleRepository.getSchedules(dependentId).first() else emptyList()
                val allCycleLogs = if(isPremium) cycleRepository.getDailyLogs(dependentId, startDate.minusMonths(6)).first() else emptyList()

                val dosesInRange = allDoses.filter { it.timestamp.toLocalDate() in startDate..endDate }
                val notesInRange = allNotes.filter { it.timestamp.toLocalDate() in startDate..endDate && finalOptions.includedNoteTypes.contains(it.type) }
                val schedulesInRange = allSchedules.filter { it.timestamp.toLocalDate() in startDate..endDate }

                val generator = PdfReportGenerator(application)
                val reportFile = generator.createReport(
                    cuidador = cuidador,
                    dependente = dependente,
                    medicamentos = allMeds,
                    doseHistory = dosesInRange,
                    healthNotes = notesInRange,
                    schedules = schedulesInRange,
                    dailyCycleLogs = allCycleLogs,
                    startDate = startDate,
                    endDate = endDate,
                    logoBitmap = logoBitmap,
                    isPremium = isPremium,
                    options = finalOptions
                )
                _reportState.postValue(Event(ReportGenerationState.Success(reportFile)))

            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Erro ao gerar relatório", e)
                _reportState.postValue(Event(ReportGenerationState.Error("Falha ao gerar relatório: ${e.message}")))
            } finally {
                // ✅ DISPARA O EVENTO PARA ESCONDER O DIÁLOGO, INDEPENDENTE DO RESULTADO
                _hideLoading.postValue(Event(Unit))
            }
        }
    }
}