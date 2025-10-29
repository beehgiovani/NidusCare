package com.developersbeeh.medcontrol.ui.listmedicamentos

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.data.repository.*
import com.developersbeeh.medcontrol.notifications.NotificationScheduler
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "ListMedicamentosVM"

@HiltViewModel
class ListMedicamentosViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val reminderRepository: ReminderRepository,
    private val medicationRepository: MedicationRepository,
    private val userRepository: UserRepository,
    private val userPreferences: com.developersbeeh.medcontrol.data.UserPreferences,
    private val activityLogRepository: ActivityLogRepository,
    private val functions: FirebaseFunctions,
    private val application: Application
) : AndroidViewModel(application) {

    private lateinit var dependentId: String
    private val _searchQuery = MutableStateFlow("")
    private val _dependentName = MutableLiveData<String>()
    val dependentName: LiveData<String> = _dependentName

    private val _doseConfirmationEvent = MutableLiveData<Event<DoseConfirmationEvent>>()
    val doseConfirmationEvent: LiveData<Event<DoseConfirmationEvent>> = _doseConfirmationEvent

    private val _allMedicationsUiState = MutableLiveData<List<MedicamentoUiState>>()
    val uiState: LiveData<List<MedicamentoUiState>>

    private val _doseTakenFeedback = MutableLiveData<Event<String>>()
    val doseTakenFeedback: LiveData<Event<String>> = _doseTakenFeedback

    private val _doseRegistrationEvent = MutableLiveData<Event<DoseRegistrationEvent>>()
    val doseRegistrationEvent: LiveData<Event<DoseRegistrationEvent>> = _doseRegistrationEvent

    private val _summaryText = MutableLiveData<String>()
    val summaryText: LiveData<String> = _summaryText

    private val _showUndoDeleteSnackbar = MutableLiveData<Event<Medicamento>>()
    val showUndoDeleteSnackbar: LiveData<Event<Medicamento>> = _showUndoDeleteSnackbar

    private val _pendingArchiveIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        uiState = _allMedicationsUiState.asFlow().combine(_searchQuery) { medList, query ->
            if (query.isBlank()) medList else medList.filter { it.medicamento.nome.contains(query, ignoreCase = true) }
        }.asLiveData()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun initialize(id: String) {
        if (this::dependentId.isInitialized && dependentId == id) return
        dependentId = id
        viewModelScope.launch {
            firestoreRepository.getDependente(id)?.let { _dependentName.postValue(it.nome) }
        }
        loadUiStateAndScheduleNotifications()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadUiStateAndScheduleNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                medicationRepository.getMedicamentos(dependentId)
                    .combine(_pendingArchiveIds) { medicamentos, pendingIds ->
                        medicamentos.filter { !it.isArchived || pendingIds.contains(it.id) }
                            .map { med ->
                                med to pendingIds.contains(med.id)
                            }
                    }
                    .combine(medicationRepository.getDoseHistory(dependentId)) { medPairs, allDoseHistory ->

                        val medsFiltrados = medPairs.map { it.first }.filter { !it.isUsoEsporadico }
                        val scheduler = NotificationScheduler(getApplication())
                        val depName = _dependentName.value ?: ""
                        val areDoseRemindersEnabled = userPreferences.getDoseRemindersEnabled()

                        medsFiltrados.forEach { med ->
                            if (areDoseRemindersEnabled) {
                                scheduler.schedule(med, dependentId, depName, allDoseHistory)
                            } else {
                                scheduler.cancelAllNotifications(med, dependentId)
                            }
                        }

                        val lembretes = reminderRepository.getReminders(dependentId).firstOrNull() ?: emptyList()
                        updateSummary(medsFiltrados, allDoseHistory, lembretes)

                        medPairs.map { (medicamento, isPending) ->
                            val status = DoseTimeCalculator.getStatusForMedicamento(medicamento, allDoseHistory)
                            val (taken, expected, adherenceStatus) = DoseTimeCalculator.calculateAdherenceForToday(medicamento, allDoseHistory)
                            val (ultimoLocal, proximoLocal) = DoseTimeCalculator.calculateLocais(medicamento, allDoseHistory)

                            MedicamentoUiState(
                                medicamento = medicamento,
                                status = status.first,
                                statusText = status.second,
                                dosesTomadasHoje = taken,
                                dosesEsperadasHoje = expected,
                                adherenceStatus = adherenceStatus,
                                ultimoLocalAplicado = ultimoLocal,
                                proximoLocalSugerido = proximoLocal,
                                isPendingArchive = isPending
                            )
                        }.sortedBy { it.status == MedicamentoStatus.FINALIZADO }
                    }.catch { e ->
                        Log.e(TAG, "Erro no fluxo de combine: ${e.message}", e)
                    }.collect { uiStateList ->
                        _allMedicationsUiState.postValue(uiStateList)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao iniciar o carregamento do UI State: ${e.message}", e)
            }
        }
    }

    private fun updateSummary(medicamentos: List<Medicamento>, allDoseHistory: List<DoseHistory>, reminders: List<Reminder>) {
        val dosesEsperadasHoje = medicamentos.sumOf { DoseTimeCalculator.calculateAdherenceForToday(it, allDoseHistory).second }
        val lembretesAtivosHoje = reminders.count { it.isActive }
        val parts = mutableListOf<String>()
        if (dosesEsperadasHoje > 0) parts.add(
            if (dosesEsperadasHoje > 1) application.getString(R.string.doses_plural, dosesEsperadasHoje)
            else application.getString(R.string.doses_singular, dosesEsperadasHoje)
        )
        if (lembretesAtivosHoje > 0) parts.add(
            if (lembretesAtivosHoje > 1) application.getString(R.string.reminders_plural, lembretesAtivosHoje)
            else application.getString(R.string.reminders_singular, lembretesAtivosHoje)
        )

        val summary = when (parts.size) {
            0 -> application.getString(R.string.no_scheduled_tasks_today)
            1 -> application.getString(R.string.today_you_have_summary, parts[0])
            else -> application.getString(R.string.today_you_have_summary_plural, parts[0], parts[1])
        }
        _summaryText.postValue(summary)
    }

    fun markDoseAsTaken(medicamento: Medicamento, uiState: MedicamentoUiState) {
        viewModelScope.launch(Dispatchers.IO) {
            when (uiState.status) {
                MedicamentoStatus.ATRASADO -> {
                    val lastScheduledDose = DoseTimeCalculator.getLastScheduledDoseBeforeNow(medicamento)
                    if (lastScheduledDose != null) {
                        _doseConfirmationEvent.postValue(Event(DoseConfirmationEvent.ConfirmLateDoseLogging(medicamento, lastScheduledDose)))
                    }
                }
                MedicamentoStatus.PROXIMA_DOSE -> {
                    val allDoseHistory = medicationRepository.getDoseHistory(dependentId).firstOrNull() ?: emptyList()
                    val nextFutureDose = DoseTimeCalculator.calculateNextDoseTime(medicamento, allDoseHistory)
                    if (nextFutureDose != null) {
                        _doseRegistrationEvent.postValue(Event(DoseRegistrationEvent.ShowEarlyDoseReasonDialog(medicamento, nextFutureDose)))
                    }
                }
                MedicamentoStatus.ESPORADICO -> {
                    _doseConfirmationEvent.postValue(Event(DoseConfirmationEvent.ConfirmSporadicDose(medicamento)))
                }
                MedicamentoStatus.FINALIZADO -> {
                    if (medicamento.isUsoContinuo || medicamento.dataInicioTratamento.plusDays(medicamento.duracaoDias - 1L).isAfter(LocalDate.now())) {
                        _doseConfirmationEvent.postValue(Event(DoseConfirmationEvent.ConfirmExtraDose(medicamento)))
                    } else {
                        _doseTakenFeedback.postValue(Event(application.getString(R.string.treatment_finished_feedback)))
                    }
                }
                else -> {
                    _doseTakenFeedback.postValue(Event(application.getString(R.string.cannot_register_dose_now)))
                }
            }
        }
    }

    fun skipDose(medicamento: Medicamento, reason: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val doseToSkipTime = DoseTimeCalculator.getNextOrLastDoseTime(medicamento)
            if (doseToSkipTime == null) {
                _doseTakenFeedback.postValue(Event(application.getString(R.string.no_dose_to_skip)))
                return@launch
            }

            val result = medicationRepository.recordSkippedDose(dependentId, medicamento, reason, doseToSkipTime)

            if (result.isSuccess) {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val formattedTime = doseToSkipTime.format(timeFormatter)
                _doseTakenFeedback.postValue(Event(application.getString(R.string.dose_skipped_success, medicamento.nome, formattedTime)))
                activityLogRepository.saveLog(dependentId, "pulou a dose de ${medicamento.nome} das $formattedTime", TipoAtividade.DOSE_REGISTRADA)
            } else {
                _doseTakenFeedback.postValue(Event(application.getString(R.string.dose_skipped_fail)))
            }
        }
    }

    fun logLateDoseNow(medicamento: Medicamento, scheduledTime: LocalDateTime) {
        viewModelScope.launch(Dispatchers.IO) {
            proceedToRecordDose(medicamento, null, null, "Dose registrada com atraso.", LocalDateTime.now())
            if (DoseTimeCalculator.isDoseVeryLate(medicamento)) {
                val hoursLate = Duration.between(scheduledTime, LocalDateTime.now()).toHours()
                _doseConfirmationEvent.postValue(Event(DoseConfirmationEvent.ConfirmLateDose(medicamento, scheduledTime, hoursLate)))
            }
        }
    }

    fun logForgottenDoseAtPastTime(medicamento: Medicamento, actualTime: LocalDateTime) {
        viewModelScope.launch(Dispatchers.IO) {
            proceedToRecordDose(medicamento, null, null, "Dose esquecida (registrada depois).", actualTime)
        }
    }

    fun readjustSchedule(medicamento: Medicamento, lateDoseTime: LocalDateTime) {
        viewModelScope.launch(Dispatchers.IO) {
            val originalScheduleTime = lateDoseTime.toLocalTime()
            val timeTakenNow = LocalTime.now()
            val diffMinutes = Duration.between(originalScheduleTime, timeTakenNow).toMinutes()
            val newHorarios = medicamento.horarios.map { horario ->
                if (horario.isAfter(originalScheduleTime)) {
                    horario.plusMinutes(diffMinutes)
                } else {
                    horario
                }
            }.sorted()
            val updatedMedicamento = medicamento.copy(horarios = newHorarios)
            val result = medicationRepository.saveMedicamento(dependentId, updatedMedicamento)
            if (result.isSuccess) {
                _doseTakenFeedback.postValue(Event(application.getString(R.string.med_schedule_adjusted, medicamento.nome)))
                activityLogRepository.saveLog(dependentId, "reajustou os horários de ${medicamento.nome}", TipoAtividade.MEDICAMENTO_EDITADO)
            } else {
                _doseTakenFeedback.postValue(Event(application.getString(R.string.schedule_adjust_fail)))
            }
        }
    }

    fun confirmEarlyDoseWithReason(medicamento: Medicamento, nextDoseTimeToCancel: LocalDateTime, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val scheduler = NotificationScheduler(getApplication())
            scheduler.cancelSpecificNotification(medicamento, nextDoseTimeToCancel, dependentId, _dependentName.value ?: "")
            val notas = if (reason.isNotBlank()) application.getString(R.string.dose_recorded_advanced_with_reason, reason)
            else application.getString(R.string.dose_recorded_advanced)
            proceedToRecordDose(medicamento, null, null, notas)
        }
    }

    fun confirmDoseTaking(medicamento: Medicamento, nextDoseTimeToCancel: LocalDateTime?, quantidade: Double?, glicemia: Double? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            if (nextDoseTimeToCancel != null) {
                val scheduler = NotificationScheduler(getApplication())
                scheduler.cancelSpecificNotification(medicamento, nextDoseTimeToCancel, dependentId, _dependentName.value ?: "")
            }
            proceedToRecordDose(medicamento, quantidade, glicemia, null)
        }
    }

    fun confirmDoseWithNote(medicamento: Medicamento, nextDoseTimeToCancel: LocalDateTime, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val scheduler = NotificationScheduler(getApplication())
            scheduler.cancelSpecificNotification(medicamento, nextDoseTimeToCancel, dependentId, _dependentName.value ?: "")
            val notas = if (note.isNotBlank()) application.getString(R.string.dose_recorded_very_advanced_with_reason, note)
            else application.getString(R.string.dose_recorded_very_advanced)
            proceedToRecordDose(medicamento, null, null, notas)
        }
    }

    private suspend fun proceedToRecordDose(medicamento: Medicamento, quantidade: Double?, glicemia: Double?, notas: String?, doseTime: LocalDateTime = LocalDateTime.now()) {
        if ((medicamento.tipo == TipoMedicamento.TOPICO || medicamento.tipo == TipoMedicamento.INJETAVEL) && medicamento.locaisDeAplicacao.isNotEmpty()) {
            try {
                val historico = medicationRepository.getDoseHistory(dependentId).first()
                val (_, proximoLocal) = DoseTimeCalculator.calculateLocais(medicamento, historico)
                _doseRegistrationEvent.postValue(Event(DoseRegistrationEvent.ShowLocationSelector(medicamento, proximoLocal, quantidade, glicemia, notas)))
            } catch (e: Exception) {
                _doseTakenFeedback.postValue(Event(application.getString(R.string.error_fetching_history)))
            }
        } else {
            confirmDoseWithDetails(medicamento, null, quantidade, glicemia, notas, doseTime)
        }
    }

    fun confirmDoseWithDetails(medicamento: Medicamento, local: String?, quantidade: Double?, glicemia: Double?, notas: String?, doseTime: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = medicationRepository.recordDoseAndUpdateStock(dependentId, medicamento, local, quantidade, notas, doseTime)
            if (result.isSuccess) {
                val doseInfo = if (quantidade != null) "$quantidade ${medicamento.unidadeDeEstoque}" else application.getString(R.string.dose_info_registered_default)
                val localInfo = if (local != null) application.getString(R.string.dose_info_registered_location, local) else ""
                activityLogRepository.saveLog(dependentId, "registrou $doseInfo de ${medicamento.nome}$localInfo", TipoAtividade.DOSE_REGISTRADA)
                _doseTakenFeedback.postValue(Event(application.getString(R.string.dose_info_registered, doseInfo, medicamento.nome, localInfo)))
            } else {
                _doseTakenFeedback.postValue(Event(application.getString(R.string.dose_registration_fail)))
            }
            if (medicamento.tipoDosagem == TipoDosagem.CALCULADA && glicemia != null) {
                val healthNote = HealthNote(
                    dependentId = dependentId,
                    userId = userRepository.getCurrentUser()?.uid ?: "dependent_user",
                    type = HealthNoteType.BLOOD_SUGAR,
                    values = mapOf("sugarLevel" to glicemia.toString()),
                    note = application.getString(R.string.note_auto_glycemia, medicamento.nome)
                ).apply { timestamp = doseTime }
                firestoreRepository.saveHealthNote(dependentId, healthNote)
            }
        }
    }

    fun calculateInsulinDose(medicamento: Medicamento, glicemiaAtual: Double, carboidratos: Double): Int {
        val alvo = medicamento.glicemiaAlvo ?: 120.0
        val fator = medicamento.fatorSensibilidade ?: 0.0
        val ratio = medicamento.ratioCarboidrato ?: 0.0
        var doseCorrecao = 0.0
        if (fator > 0 && glicemiaAtual > alvo) {
            doseCorrecao = (glicemiaAtual - alvo) / fator
        }
        var doseCarboidrato = 0.0
        if (ratio > 0 && carboidratos > 0) {
            doseCarboidrato = carboidratos / ratio
        }
        val doseTotal = doseCorrecao + doseCarboidrato
        return if (doseTotal > 0) doseTotal.roundToInt() else 0
    }

    fun confirmSporadicDoseTaken(medicamento: Medicamento) {
        viewModelScope.launch(Dispatchers.IO) {
            when (medicamento.tipoDosagem) {
                TipoDosagem.FIXA -> proceedToRecordDose(medicamento, null, null, null)
                TipoDosagem.MANUAL -> _doseRegistrationEvent.postValue(Event(DoseRegistrationEvent.ShowManualDoseInput(medicamento)))
                TipoDosagem.CALCULADA -> _doseRegistrationEvent.postValue(Event(DoseRegistrationEvent.ShowCalculatedDoseInput(medicamento)))
            }
        }
    }

    fun addStockLot(medicamento: Medicamento, novoLote: EstoqueLote) {
        viewModelScope.launch(Dispatchers.IO) {
            medicationRepository.addStockLot(dependentId, medicamento.id, novoLote)
        }
    }

    fun deleteMedicamento(medicamento: Medicamento) {
        viewModelScope.launch(Dispatchers.IO) {
            _pendingArchiveIds.update { it + medicamento.id }

            val scheduler = NotificationScheduler(getApplication())
            scheduler.cancelAllNotifications(medicamento, dependentId)
            val result = medicationRepository.archiveMedicamento(dependentId, medicamento.id)
            if (result.isSuccess) {
                _showUndoDeleteSnackbar.postValue(Event(medicamento))
            } else {
                _pendingArchiveIds.update { it - medicamento.id }
                _doseTakenFeedback.postValue(Event(application.getString(R.string.error_deleting_medication)))
            }
        }
    }

    fun undoDeleteMedicamento(medicamento: Medicamento) {
        viewModelScope.launch(Dispatchers.IO) {
            _pendingArchiveIds.update { it - medicamento.id }
            medicationRepository.unarchiveMedicamento(dependentId, medicamento.id).onSuccess {
                _doseTakenFeedback.postValue(Event(application.getString(R.string.medication_restored, medicamento.nome)))
            }
        }
    }

    fun permanentlyDeleteMedicamento(medicamento: Medicamento) {
        viewModelScope.launch(Dispatchers.IO) {
            _pendingArchiveIds.update { it - medicamento.id }
            val result = medicationRepository.permanentlyDeleteMedicamento(dependentId, medicamento.id)
            if (result.isSuccess) {
                activityLogRepository.saveLog(dependentId, "excluiu permanentemente o medicamento ${medicamento.nome}", TipoAtividade.MEDICAMENTO_EXCLUIDO)
            }
        }
    }

    fun togglePauseState(medicamento: Medicamento) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMedicamento = medicamento.copy(isPaused = !medicamento.isPaused)
            val result = medicationRepository.saveMedicamento(dependentId, updatedMedicamento)
            if(result.isSuccess){
                val acao = if(updatedMedicamento.isPaused) application.getString(R.string.medication_action_paused) else application.getString(R.string.medication_action_resumed)
                val tipoAtividade = if(updatedMedicamento.isPaused) TipoAtividade.TRATAMENTO_PAUSADO else TipoAtividade.TRATAMENTO_REATIVADO
                activityLogRepository.saveLog(dependentId, application.getString(R.string.medication_action_log, acao, medicamento.nome), tipoAtividade)
            }
        }
    }

    fun generateShareableScheduleForToday(): String {
        val context = getApplication<Application>().applicationContext
        val medicationsUiState = _allMedicationsUiState.value ?: return context.getString(R.string.no_meds_to_share)
        val medicationsForToday = medicationsUiState
            .filter { it.status != MedicamentoStatus.FINALIZADO && it.status != MedicamentoStatus.PAUSADO }
            .map { it.medicamento }
            .filter { it.horarios.isNotEmpty() }
        val depName = _dependentName.value ?: context.getString(R.string.share_patient_placeholder)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")))
        if (medicationsForToday.isEmpty()) {
            return context.getString(R.string.share_schedule_no_meds_today, depName)
        }
        val scheduleBuilder = StringBuilder()
        scheduleBuilder.appendLine(context.getString(R.string.share_schedule_header, depName, today))
        scheduleBuilder.appendLine("-----------------------------------")
        medicationsForToday
            .flatMap { med -> med.horarios.map { horario -> Pair(horario, med) } }
            .sortedBy { it.first }
            .forEach { (horario, med) ->
                var doseInfo = med.dosagem
                if (med.tipoDosagem == TipoDosagem.MANUAL) {
                    doseInfo = context.getString(R.string.share_dose_type_manual)
                } else if (med.tipoDosagem == TipoDosagem.CALCULADA) {
                    doseInfo = context.getString(R.string.share_dose_type_calculated)
                }
                scheduleBuilder.appendLine("⏰ *${horario.format(DateTimeFormatter.ofPattern("HH:mm"))}* - ${med.nome} ($doseInfo)")
            }
        scheduleBuilder.appendLine(context.getString(R.string.share_schedule_footer))
        return scheduleBuilder.toString()
    }
}