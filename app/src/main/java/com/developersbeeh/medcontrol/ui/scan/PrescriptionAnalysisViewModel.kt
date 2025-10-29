package com.developersbeeh.medcontrol.ui.scan

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.FrequenciaTipo
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.repository.ImageAnalysisRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

private const val TAG = "PrescriptionAnalysisVM"

sealed class PrescriptionAnalysisUiState {
    data class Success(val medications: List<Medicamento>) : PrescriptionAnalysisUiState()
    data class Error(val message: String) : PrescriptionAnalysisUiState()
}

@HiltViewModel
class PrescriptionAnalysisViewModel @Inject constructor(
    private val imageAnalysisRepository: ImageAnalysisRepository,
    private val medicationRepository: MedicationRepository,
    private val application: Application // Injetado
) : ViewModel() {

    private val _uiState = MutableLiveData<PrescriptionAnalysisUiState>()
    val uiState: LiveData<PrescriptionAnalysisUiState> = _uiState

    private var _medicationsFromAnalysis = mutableListOf<Medicamento>()
    private val _incompleteMedicationsQueue = ArrayDeque<Medicamento>()

    private val _saveStatus = MutableLiveData<Event<Boolean>>()
    val saveStatus: LiveData<Event<Boolean>> = _saveStatus

    private val _requestStartTimeEvent = MutableLiveData<Event<Medicamento>>()
    val requestStartTimeEvent: LiveData<Event<Medicamento>> = _requestStartTimeEvent

    private val _showLoading = MutableLiveData<Event<String>>()
    val showLoading: LiveData<Event<String>> = _showLoading

    private val _hideLoading = MutableLiveData<Event<Unit>>()
    val hideLoading: LiveData<Event<Unit>> = _hideLoading

    private lateinit var dependentId: String
    private lateinit var dependentName: String

    fun initialize(dependentId: String, imageUriString: String, dependentName: String) {
        if (this::dependentId.isInitialized) return

        this.dependentId = dependentId
        this.dependentName = dependentName
        val imageUri = Uri.parse(imageUriString)
        startAnalysis(imageUri)
    }

    private fun startAnalysis(imageUri: Uri) {
        _showLoading.value = Event(application.getString(R.string.analyzing_prescription))
        viewModelScope.launch {
            try {
                val result = imageAnalysisRepository.analyzePrescription(dependentId, imageUri)
                result.onSuccess { medications ->
                    _medicationsFromAnalysis.clear()
                    _medicationsFromAnalysis.addAll(medications)
                    _uiState.postValue(PrescriptionAnalysisUiState.Success(_medicationsFromAnalysis.toList()))
                }.onFailure { e ->
                    _uiState.postValue(PrescriptionAnalysisUiState.Error(e.message ?: application.getString(R.string.error_unknown_prescription_analysis)))
                    Log.e(TAG, "Erro ao analisar receita", e)
                }
            } finally {
                _hideLoading.postValue(Event(Unit))
            }
        }
    }

    fun removeMedicamentoFromList(medicamento: Medicamento) {
        _medicationsFromAnalysis.remove(medicamento)
        _uiState.value = PrescriptionAnalysisUiState.Success(_medicationsFromAnalysis.toList())
    }

    fun saveAllMedications() {
        _showLoading.value = Event(application.getString(R.string.saving_medications))
        viewModelScope.launch {
            try {
                val validMeds = _medicationsFromAnalysis.filter { it.horarios.isNotEmpty() || it.isUsoEsporadico }
                val incompleteMeds = _medicationsFromAnalysis.filter { it.horarios.isEmpty() && !it.isUsoEsporadico }

                var allSuccessful = true
                for (med in validMeds) {
                    val result = medicationRepository.saveMedicamento(dependentId, med)
                    if (result.isFailure) allSuccessful = false
                }

                if (incompleteMeds.isNotEmpty()) {
                    _incompleteMedicationsQueue.clear()
                    _incompleteMedicationsQueue.addAll(incompleteMeds)
                    _requestStartTimeEvent.postValue(Event(_incompleteMedicationsQueue.removeFirst()))
                } else {
                    _saveStatus.postValue(Event(allSuccessful))
                }
            } finally {
                if (_incompleteMedicationsQueue.isEmpty()) {
                    _hideLoading.postValue(Event(Unit))
                }
            }
        }
    }

    fun saveIncompleteMedication(medicamento: Medicamento, startTime: LocalTime) {
        viewModelScope.launch {
            val horarios = generateSchedule(startTime, medicamento.frequenciaTipo, medicamento.frequenciaValor)
            val updatedMed = medicamento.copy(horarios = horarios)

            val result = medicationRepository.saveMedicamento(dependentId, updatedMed)
            if (result.isFailure) {
                Log.e(TAG, "Falha ao salvar medicamento incompleto: ${medicamento.nome}")
            }

            if (_incompleteMedicationsQueue.isNotEmpty()) {
                _requestStartTimeEvent.postValue(Event(_incompleteMedicationsQueue.removeFirst()))
            } else {
                _hideLoading.postValue(Event(Unit))
                _saveStatus.postValue(Event(true))
            }
        }
    }

    private fun generateSchedule(startTime: LocalTime, frequenciaTipo: FrequenciaTipo, frequenciaValor: Int): List<LocalTime> {
        val horarios = mutableListOf<LocalTime>()
        if (frequenciaTipo == FrequenciaTipo.DIARIA && frequenciaValor > 0) {
            val intervalInMinutes = (24 * 60) / frequenciaValor
            for (i in 0 until frequenciaValor) {
                val time = startTime.plusMinutes((i * intervalInMinutes).toLong())
                horarios.add(time)
            }
        } else {
            horarios.add(startTime)
        }
        return horarios.sorted()
    }
}