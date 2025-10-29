// src/main/java/com/developersbeeh/medcontrol/ui/schedule/HealthScheduleViewModel.kt
package com.developersbeeh.medcontrol.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.developersbeeh.medcontrol.data.repository.ScheduleRepository
import com.developersbeeh.medcontrol.util.Event
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private lateinit var dependentId: String
    private lateinit var dependentName: String

    private val _schedules = MutableLiveData<UiState<List<AgendamentoSaude>>>()
    val schedules: LiveData<UiState<List<AgendamentoSaude>>> = _schedules

    private val _navigationEvent = MutableLiveData<Event<NavDirections>>()
    val navigationEvent: LiveData<Event<NavDirections>> = _navigationEvent

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    fun initialize(dependentId: String, dependentName: String) {
        if (this::dependentId.isInitialized && this.dependentId == dependentId) return
        this.dependentId = dependentId
        this.dependentName = dependentName
        loadSchedules()
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            _schedules.value = UiState.Loading
            try {
                scheduleRepository.getSchedules(dependentId).collectLatest { scheduleList ->
                    _schedules.postValue(UiState.Success(scheduleList))
                }
            } catch (e: Exception) {
                _schedules.postValue(UiState.Error("Falha ao carregar agendamentos: ${e.message}"))
            }
        }
    }

    fun onAddScheduleClicked() {
        val action = HealthScheduleFragmentDirections
            .actionHealthScheduleFragmentToAddEditScheduleFragment(
                dependentId = dependentId,
                dependentName = dependentName,
                agendamento = null
            )
        _navigationEvent.value = Event(action)
    }

    fun onScheduleSelected(agendamento: AgendamentoSaude) {
        val action = HealthScheduleFragmentDirections
            .actionHealthScheduleFragmentToAddEditScheduleFragment(
                dependentId = dependentId,
                dependentName = dependentName,
                agendamento = agendamento
            )
        _navigationEvent.value = Event(action)
    }

    fun deleteSchedule(agendamento: AgendamentoSaude) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(agendamento).onSuccess {
                _actionFeedback.postValue(Event("Agendamento excluído com sucesso."))
            }.onFailure {
                _actionFeedback.postValue(Event("Falha ao excluir agendamento."))
            }
        }
    }
}