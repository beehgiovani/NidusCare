package com.developersbeeh.medcontrol.ui.dosehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.RecordedDoseStatus
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.CalculatedDoseStatus
import com.developersbeeh.medcontrol.util.DoseStatusCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DoseHistoryFilter(val dependentId: String, val medicationId: String? = null)

@HiltViewModel
class DoseHistoryViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow<DoseHistoryFilter?>(null)
    private val _allMedications = MutableStateFlow<List<Medicamento>>(emptyList())
    val allMedications: StateFlow<List<Medicamento>> = _allMedications

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyPagerFlow: Flow<PagingData<DoseHistoryListItem>> = _filter.filterNotNull().flatMapLatest { filter ->
        val allDosesFlow = medicationRepository.getDoseHistory(filter.dependentId)

        allDosesFlow.flatMapLatest { allDoses ->
            firestoreRepository.getDoseHistoryPager(filter.dependentId, filter.medicationId).flow
                .map { pagingData ->
                    pagingData.map { dose ->
                        val med = _allMedications.value.find { it.id == dose.medicamentoId }
                        val medName = med?.nome ?: "Medicamento"

                        // ✅ CORREÇÃO: A lógica agora checa o status 'SKIPPED' do DB
                        val calculatedStatus = if (med != null) {
                            if (dose.status == RecordedDoseStatus.SKIPPED) {
                                CalculatedDoseStatus.SKIPPED
                            } else {
                                DoseStatusCalculator.calculateDoseStatus(dose, med, allDoses)
                            }
                        } else {
                            CalculatedDoseStatus.SPORADIC
                        }

                        DoseHistoryListItem.DoseItem(dose, medName, calculatedStatus)
                    }
                }
                .map { pagingData ->
                    pagingData.insertSeparators { before, after ->
                        val beforeDate = (before as? DoseHistoryListItem.DoseItem)?.dose?.timestamp?.toLocalDate()
                        val afterDate = (after as? DoseHistoryListItem.DoseItem)?.dose?.timestamp?.toLocalDate()

                        if (afterDate != null && (beforeDate == null || beforeDate != afterDate)) {
                            DoseHistoryListItem.HeaderItem(afterDate)
                        } else {
                            null
                        }
                    }
                }
        }
    }.cachedIn(viewModelScope)

    fun initialize(dependentId: String) {
        if (_filter.value?.dependentId == dependentId) return
        _filter.value = DoseHistoryFilter(dependentId = dependentId)
        loadAllMedications(dependentId)
    }

    private fun loadAllMedications(dependentId: String) {
        viewModelScope.launch {
            _allMedications.value = medicationRepository.getMedicamentos(dependentId).first()
        }
    }

    fun applyFilter(medicationId: String?) {
        _filter.value?.let { currentFilter ->
            _filter.value = currentFilter.copy(medicationId = medicationId)
        }
    }
}