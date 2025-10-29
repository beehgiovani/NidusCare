package com.developersbeeh.medcontrol.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.developersbeeh.medcontrol.data.model.TipoAgendamento
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
import com.developersbeeh.medcontrol.data.repository.ScheduleRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddEditScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val activityLogRepository: ActivityLogRepository // ✅ DEPENDÊNCIAS ANTIGAS REMOVIDAS, NOVA ADICIONADA
) : ViewModel() {

    private val _saveStatus = MutableLiveData<Event<Result<Unit>>>()
    val saveStatus: LiveData<Event<Result<Unit>>> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ✅ FUNÇÃO saveLog LOCAL REMOVIDA

    fun saveSchedule(
        dependentId: String,
        agendamentoToEdit: AgendamentoSaude?,
        titulo: String,
        tipo: TipoAgendamento,
        data: LocalDate,
        hora: LocalTime,
        local: String?,
        profissional: String?,
        notas: String?,
        lembretes: List<Int>
    ) {
        if (titulo.isBlank()) {
            _saveStatus.value = Event(Result.failure(IllegalArgumentException("O título é obrigatório.")))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val dateTime = data.atTime(hora)

            val agendamento = agendamentoToEdit?.copy(
                titulo = titulo,
                tipo = tipo,
                local = local,
                profissional = profissional,
                notasDePreparo = notas,
                lembretes = lembretes
            ) ?: AgendamentoSaude(
                dependentId = dependentId,
                titulo = titulo,
                tipo = tipo,
                local = local,
                profissional = profissional,
                notasDePreparo = notas,
                lembretes = lembretes
            )
            agendamento.timestamp = dateTime

            val result = scheduleRepository.saveSchedule(agendamento)
            if (result.isSuccess && agendamentoToEdit == null) {
                // ✅ CHAMADA ATUALIZADA PARA USAR O REPOSITÓRIO CENTRALIZADO
                activityLogRepository.saveLog(dependentId, "criou o agendamento '${agendamento.titulo}'", TipoAtividade.AGENDAMENTO_CRIADO)
            }
            _saveStatus.postValue(Event(result))
            _isLoading.postValue(false)
        }
    }
}