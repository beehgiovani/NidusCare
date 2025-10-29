package com.developersbeeh.medcontrol.ui.vaccination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.RegistroVacina
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.developersbeeh.medcontrol.data.model.Vacina
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
import com.developersbeeh.medcontrol.data.repository.VaccineRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import javax.inject.Inject

enum class VacinaStatus { TOMADA, ATRASADA, PROXIMA, OK }

data class VacinaUiItem(
    val vacina: Vacina,
    val registro: RegistroVacina?,
    val status: VacinaStatus
)

@HiltViewModel
class VaccinationViewModel @Inject constructor(
    private val vaccineRepository: VaccineRepository,
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private lateinit var dependentId: String
    private var birthDate: LocalDate? = null

    private val _groupedVaccines = MutableLiveData<Map<Int, List<VacinaUiItem>>>()
    val groupedVaccines: LiveData<Map<Int, List<VacinaUiItem>>> = _groupedVaccines

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    fun initialize(dependentId: String, dataNascimento: String) {
        if (this::dependentId.isInitialized && this.dependentId == dependentId) return
        this.dependentId = dependentId
        this.birthDate = parseDate(dataNascimento)

        viewModelScope.launch {
            vaccineRepository.getRegistrosVacina(dependentId).collectLatest { registros ->
                processVaccineData(registros)
            }
        }
    }

    private fun processVaccineData(registros: List<RegistroVacina>) {
        val calendario = vaccineRepository.getCalendarioVacinal()
        val ageInMonths = birthDate?.let { Period.between(it, LocalDate.now()).toTotalMonths().toInt() } ?: -1

        val uiItems = calendario.map { vacina ->
            val registro = registros.find { it.vacinaId == vacina.id }
            val status = determineStatus(vacina, registro, ageInMonths)
            VacinaUiItem(vacina, registro, status)
        }

        _groupedVaccines.postValue(uiItems.groupBy { it.vacina.idadeRecomendadaMeses }.toSortedMap())
    }

    private fun determineStatus(vacina: Vacina, registro: RegistroVacina?, ageInMonths: Int): VacinaStatus {
        return when {
            registro != null -> VacinaStatus.TOMADA
            ageInMonths >= vacina.idadeRecomendadaMeses -> VacinaStatus.ATRASADA
            ageInMonths + 1 == vacina.idadeRecomendadaMeses -> VacinaStatus.PROXIMA
            else -> VacinaStatus.OK
        }
    }

    fun saveVaccineRecord(vacina: Vacina, lote: String?, local: String?, notas: String?) {
        viewModelScope.launch {
            val registro = RegistroVacina(
                vacinaId = vacina.id,
                lote = lote,
                localAplicacao = local,
                notas = notas
            ).apply {
                // ✅ CORREÇÃO: Usamos o novo setter para definir a data e hora atuais.
                timestamp = LocalDateTime.now()
            }

            val result = vaccineRepository.saveRegistroVacina(dependentId, registro)
            if (result.isSuccess) {
                activityLogRepository.saveLog(dependentId, "registrou a vacina '${vacina.nome}'", TipoAtividade.ANOTACAO_CRIADA)
                _actionFeedback.postValue(Event("Vacina '${vacina.nome}' registrada com sucesso!"))
            } else {
                _actionFeedback.postValue(Event("Erro ao registrar a vacina."))
            }
        }
    }

    private fun parseDate(dateString: String): LocalDate? {
        if (dateString.isBlank()) return null
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
        )
        for (formatter in formatters) {
            try {
                return LocalDate.parse(dateString, formatter)
            } catch (e: DateTimeParseException) {
                // Tenta o próximo
            }
        }
        return null
    }
}