package com.developersbeeh.medcontrol.ui.archive

import android.app.Application
import androidx.lifecycle.*
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ArchivedMedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val application: Application // Injetado
) : AndroidViewModel(application) { // Herda de AndroidViewModel

    private val _dependentId = MutableLiveData<String>()

    val actionFeedback = MutableLiveData<Event<String>>()

    val archivedMedications: LiveData<List<Medicamento>> = _dependentId.switchMap { id ->
        if (id.isNotBlank()) {
            medicationRepository.getArchivedMedicamentos(id).asLiveData()
        } else {
            MutableLiveData(emptyList())
        }
    }

    val finishedTreatments: LiveData<List<Medicamento>> = archivedMedications.map { list ->
        list.filter {
            !it.isUsoContinuo && it.duracaoDias > 0 &&
                    LocalDate.now().isAfter(it.dataInicioTratamento.plusDays(it.duracaoDias.toLong() - 1))
        }
    }

    val zeroStockMeds: LiveData<List<Medicamento>> = archivedMedications.map { list ->
        list.filter {
            (it.lotes.isNotEmpty() || it.nivelDeAlertaEstoque > 0) && it.estoqueAtualTotal <= 0 && it.lotes.none { lote -> lote.dataValidade.isBefore(LocalDate.now()) }
        }
    }

    val expiredStockMeds: LiveData<List<Medicamento>> = archivedMedications.map { list ->
        list.filter { med ->
            med.lotes.any { it.dataValidade.isBefore(LocalDate.now()) }
        }
    }

    fun initialize(depId: String) {
        if (_dependentId.value != depId) {
            _dependentId.value = depId
        }
    }

    fun restoreMedicamento(medicamento: Medicamento) = viewModelScope.launch {
        _dependentId.value?.let { depId ->
            medicationRepository.unarchiveMedicamento(depId, medicamento.id).onSuccess {
                activityLogRepository.saveLog(depId, application.getString(R.string.log_med_restored, medicamento.nome), TipoAtividade.TRATAMENTO_REATIVADO)
                actionFeedback.postValue(Event(application.getString(R.string.feedback_med_restored, medicamento.nome)))
            }.onFailure {
                actionFeedback.postValue(Event(application.getString(R.string.feedback_med_restore_fail)))
            }
        }
    }

    fun removeExpiredLots(medicamento: Medicamento) = viewModelScope.launch {
        _dependentId.value?.let { depId ->
            val validLotes = medicamento.lotes.filter { it.dataValidade.isAfter(LocalDate.now().minusDays(1)) }
            val updatedMed = medicamento.copy(lotes = validLotes)
            medicationRepository.saveMedicamento(depId, updatedMed).onSuccess {
                activityLogRepository.saveLog(depId, application.getString(R.string.log_expired_lots_removed, medicamento.nome), TipoAtividade.MEDICAMENTO_EDITADO)
                actionFeedback.postValue(Event(application.getString(R.string.feedback_expired_lots_removed, medicamento.nome)))
            }
        }
    }

    fun stopTrackingStock(medicamento: Medicamento) = viewModelScope.launch {
        _dependentId.value?.let { depId ->
            val updatedMed = medicamento.copy(lotes = emptyList(), nivelDeAlertaEstoque = 0)
            medicationRepository.saveMedicamento(depId, updatedMed).onSuccess {
                actionFeedback.postValue(Event(application.getString(R.string.feedback_stock_tracking_stopped, medicamento.nome)))
            }
        }
    }

    fun deleteMedicationPermanently(medicamento: Medicamento) = viewModelScope.launch {
        _dependentId.value?.let { depId ->
            medicationRepository.permanentlyDeleteMedicamento(depId, medicamento.id).onSuccess {
                activityLogRepository.saveLog(depId, application.getString(R.string.log_med_deleted_permanently, medicamento.nome), TipoAtividade.MEDICAMENTO_EXCLUIDO)
                actionFeedback.postValue(Event(application.getString(R.string.feedback_med_deleted_permanently, medicamento.nome)))
            }.onFailure {
                actionFeedback.postValue(Event(application.getString(R.string.feedback_delete_permanent_fail)))
            }
        }
    }

    fun addStockLot(medicamento: Medicamento, novoLote: EstoqueLote) = viewModelScope.launch {
        _dependentId.value?.let { depId ->
            medicationRepository.addStockLot(depId, medicamento.id, novoLote).onSuccess {
                activityLogRepository.saveLog(depId, application.getString(R.string.log_stock_refilled, medicamento.nome), TipoAtividade.MEDICAMENTO_EDITADO)
                actionFeedback.postValue(Event(application.getString(R.string.feedback_stock_refilled, medicamento.nome)))
            }.onFailure {
                actionFeedback.postValue(Event(application.getString(R.string.feedback_stock_refill_fail)))
            }
        }
    }
}