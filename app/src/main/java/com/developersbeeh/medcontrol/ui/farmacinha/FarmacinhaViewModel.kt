package com.developersbeeh.medcontrol.ui.farmacinha

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FarmacinhaViewModel"

data class FarmacinhaItem(
    val medicamento: Medicamento,
    val dependenteNome: String,
    val dependentId: String
)

@HiltViewModel
class FarmacinhaViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val userPreferences: com.developersbeeh.medcontrol.data.UserPreferences
) : ViewModel() {

    private val filterState = MutableStateFlow<String?>(null)
    // ✅ CORREÇÃO: Expõe o ID selecionado para o Fragment
    val selectedDependentId: StateFlow<String?> = filterState

    private val _dependents = MutableStateFlow<List<Dependente>>(emptyList())
    val dependents: LiveData<List<Dependente>> = _dependents.asLiveData()

    private val _farmacinhaItens = MutableLiveData<UiState<List<FarmacinhaItem>>>()
    val farmacinhaItens: LiveData<UiState<List<FarmacinhaItem>>> = _farmacinhaItens

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    fun initialize(initialDependentId: String?) {
        filterState.value = initialDependentId
        loadDependentsAndFarmacinha()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadDependentsAndFarmacinha() {
        viewModelScope.launch {
            if (_dependents.value.isEmpty()) {
                try {
                    _dependents.value = firestoreRepository.getDependentes().first()
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao buscar lista de dependentes: ", e)
                }
            }

            combine(_dependents, filterState) { dependents, filterId ->
                Pair(dependents, filterId)
            }.collectLatest { (dependentsList, filterId) ->
                _farmacinhaItens.postValue(UiState.Loading)
                try {
                    val dependentsToFetch = if (filterId == null) {
                        dependentsList
                    } else {
                        dependentsList.filter { it.id == filterId }
                    }

                    val allItems = mutableListOf<FarmacinhaItem>()
                    dependentsToFetch.forEach { dependent ->
                        val meds = medicationRepository.getMedicamentos(dependent.id).first()
                        meds.filter { it.isUsoEsporadico }
                            .forEach { med ->
                                allItems.add(FarmacinhaItem(med, dependent.nome, dependent.id))
                            }
                    }

                    val sortedList = allItems.sortedBy { it.medicamento.lotes.minOfOrNull { lote -> lote.dataValidade } }
                    _farmacinhaItens.postValue(UiState.Success(sortedList))
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao buscar itens da farmacinha: ", e)
                    _farmacinhaItens.postValue(UiState.Error("Não foi possível carregar os dados."))
                }
            }
        }
    }

    fun setFilter(dependentId: String?) {
        filterState.value = dependentId
    }

    fun markDoseAsTaken(item: FarmacinhaItem) = viewModelScope.launch {
        if (userPreferences.temPermissao(PermissaoTipo.REGISTRAR_DOSE)) {
            val result = medicationRepository.recordDoseAndUpdateStock(item.dependentId, item.medicamento, null, null, null)
            if (result.isSuccess) {
                activityLogRepository.saveLog(item.dependentId, "registrou a dose de '${item.medicamento.nome}' pela farmacinha", TipoAtividade.DOSE_REGISTRADA)
                _actionFeedback.postValue(Event("Dose de '${item.medicamento.nome}' registrada!"))
            } else {
                _actionFeedback.postValue(Event("Falha ao registrar dose."))
            }
        } else {
            _actionFeedback.postValue(Event("Você não tem permissão para registrar doses."))
        }
    }

    fun addStockLot(item: FarmacinhaItem, novoLote: EstoqueLote) = viewModelScope.launch {
        val result = medicationRepository.addStockLot(item.dependentId, item.medicamento.id, novoLote)
        if (result.isSuccess) {
            val descricaoLog = "repos o estoque de '${item.medicamento.nome}'"
            activityLogRepository.saveLog(item.dependentId, descricaoLog, TipoAtividade.MEDICAMENTO_EDITADO)
            _actionFeedback.postValue(Event("Estoque de '${item.medicamento.nome}' atualizado!"))
        } else {
            _actionFeedback.postValue(Event("Falha ao atualizar estoque."))
        }
    }

    fun deleteMedicamento(item: FarmacinhaItem) = viewModelScope.launch {
        val result = medicationRepository.permanentlyDeleteMedicamento(item.dependentId, item.medicamento.id)
        if (result.isSuccess) {
            activityLogRepository.saveLog(item.dependentId, "excluiu o medicamento esporádico '${item.medicamento.nome}'", TipoAtividade.MEDICAMENTO_EXCLUIDO)
            _actionFeedback.postValue(Event("'${item.medicamento.nome}' foi excluído da farmacinha."))
        } else {
            _actionFeedback.postValue(Event("Falha ao excluir medicamento."))
        }
    }
}