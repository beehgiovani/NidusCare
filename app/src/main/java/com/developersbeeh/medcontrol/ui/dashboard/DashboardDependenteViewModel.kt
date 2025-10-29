package com.developersbeeh.medcontrol.ui.dashboard

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.data.repository.*
import com.developersbeeh.medcontrol.util.AgeCalculator
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "DashboardDependenteVM"

// Data classes para o estado da UI
data class ImcResult(val value: Float, val classification: String, val color: Int)
data class WeightGoalStatus(val progress: Int, val progressText: String, val color: Int)
data class WeeklySummary(
    val mediaDiariaAguaMl: Double,
    val totalMinutosAtividade: Int
)

// Constantes para ações que não são de navegação
const val ACTION_ID_VIEW_CREDENTIALS = -100
const val ACTION_ID_EMERGENCY = -101
const val ACTION_ID_TOGGLE_ALARM = -102
const val ACTION_ID_DELETE_DEPENDENT = -103
const val ACTION_ID_SHOW_ANALYSIS_DIALOG = -104

@HiltViewModel
class DashboardDependenteViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val userRepository: UserRepository,
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reminderRepository: ReminderRepository,
    private val userPreferences: UserPreferences,
    private val application: Application // Injetado
) : AndroidViewModel(application) { // Herda de AndroidViewModel

    private val _dependentId = MutableLiveData<String>()
    val dependente = MutableLiveData<Dependente?>()
    private val _imcResult = MutableLiveData<ImcResult?>()
    val imcResult: LiveData<ImcResult?> = _imcResult
    private val _weightGoalStatus = MutableLiveData<WeightGoalStatus?>()
    val weightGoalStatus: LiveData<WeightGoalStatus?> = _weightGoalStatus
    private val _summaryText = MutableLiveData<String>()
    val summaryText: LiveData<String> = _summaryText
    private val _weeklySummaryState = MutableLiveData<WeeklySummary?>()
    val weeklySummaryState: LiveData<WeeklySummary?> = _weeklySummaryState
    private val _careManagementCategories = MutableLiveData<List<DashboardCategory>>()
    val careManagementCategories: LiveData<List<DashboardCategory>> = _careManagementCategories
    private val _healthDataCategories = MutableLiveData<List<DashboardCategory>>()
    val healthDataCategories: LiveData<List<DashboardCategory>> = _healthDataCategories
    private val _profileManagementCategories = MutableLiveData<List<DashboardCategory>>()
    val profileManagementCategories: LiveData<List<DashboardCategory>> = _profileManagementCategories
    private val _advancedToolsCategories = MutableLiveData<List<DashboardCategory>>()
    val advancedToolsCategories: LiveData<List<DashboardCategory>> = _advancedToolsCategories
    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback
    val showAnalysisPromptDialog = MutableLiveData<Event<Unit>>()
    private val _showCredentials = MutableLiveData<Event<Pair<String, String>>>()
    val showCredentials: LiveData<Event<Pair<String, String>>> = _showCredentials
    private val _showDeleteConfirmation = MutableLiveData<Event<Unit>>()
    val showDeleteConfirmation: LiveData<Event<Unit>> = _showDeleteConfirmation
    private val _showEmergencyConfirmation = MutableLiveData<Event<Unit>>()
    val showEmergencyConfirmation: LiveData<Event<Unit>> = _showEmergencyConfirmation
    private val _remindersSummary = MutableLiveData<List<Reminder>>()
    val remindersSummary: LiveData<List<Reminder>> = _remindersSummary
    private val _missedDoseAlert = MutableLiveData<List<String>>()
    val missedDoseAlert: LiveData<List<String>> = _missedDoseAlert

    private val _showLoading = MutableLiveData<Event<String>>()
    val showLoading: LiveData<Event<String>> = _showLoading

    private val _hideLoading = MutableLiveData<Event<Unit>>()
    val hideLoading: LiveData<Event<Unit>> = _hideLoading

    fun initialize(id: String) {
        if (_dependentId.value == id) return
        _dependentId.value = id

        viewModelScope.launch {
            firestoreRepository.listenToDependentProfile(id).collectLatest { dep ->
                if (dep != null) {
                    dependente.postValue(dep)
                    buildAllCategories(dep)
                    calculateImc(dep.peso, dep.altura)
                    loadDynamicData(dep)
                } else {
                    Log.e(TAG, application.getString(R.string.error_dependent_not_found_id, id))
                }
            }
        }
    }

    private fun loadDynamicData(dep: Dependente) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val sevenDaysAgo = today.minusDays(6)

            @Suppress("UNCHECKED_CAST")
            val flows = listOf(
                medicationRepository.getMedicamentos(dep.id),
                medicationRepository.getDoseHistory(dep.id),
                scheduleRepository.getSchedules(dep.id),
                firestoreRepository.getHidratacaoHistory(dep.id, sevenDaysAgo, sevenDaysAgo),
                firestoreRepository.getAtividadeFisicaHistory(dep.id, sevenDaysAgo, sevenDaysAgo),
                firestoreRepository.getHealthNotes(dep.id),
                reminderRepository.getReminders(dep.id)
            )

            combine(flows) { dataArray ->
                val meds = dataArray[0] as? List<Medicamento> ?: emptyList()
                val doses = dataArray[1] as? List<DoseHistory> ?: emptyList()
                val schedules = dataArray[2] as? List<AgendamentoSaude> ?: emptyList()
                val hydration = dataArray[3] as? List<Hidratacao> ?: emptyList()
                val activities = dataArray[4] as? List<AtividadeFisica> ?: emptyList()
                val healthNotes = dataArray[5] as? List<HealthNote> ?: emptyList()
                val reminders = dataArray[6] as? List<Reminder> ?: emptyList()

                val missedMeds = DoseTimeCalculator.getMissedDoseMedications(meds, doses)
                _missedDoseAlert.postValue(missedMeds.map { it.nome })

                processAllData(dep, meds, doses, schedules, hydration, activities, healthNotes, reminders)
            }.catch { e ->
                Log.e(TAG, application.getString(R.string.error_combining_data_streams), e)
            }.collect()
        }
    }

    private fun processAllData(
        dep: Dependente,
        meds: List<Medicamento>,
        doses: List<DoseHistory>,
        schedules: List<AgendamentoSaude>,
        hidratacao: List<Hidratacao>,
        atividades: List<AtividadeFisica>,
        healthNotes: List<HealthNote>,
        reminders: List<Reminder>
    ) {
        calculateWeightGoalStatus(dep, healthNotes)
        createDailySummary(dep.nome, meds, doses, schedules)
        createWeeklySummary(hidratacao, atividades)

        val activeReminders = reminders.filter { it.isActive }.sortedBy { it.time }
        _remindersSummary.postValue(activeReminders)
    }

    private fun createDailySummary(dependentName: String, medicamentos: List<Medicamento>, doses: List<DoseHistory>, schedules: List<AgendamentoSaude>) {
        val now = LocalDateTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val nextDose = medicamentos
            .filter { !it.isPaused && !it.isUsoEsporadico && it.horarios.isNotEmpty() }
            .mapNotNull { med -> DoseTimeCalculator.calculateNextDoseTime(med, doses) }
            .minOrNull()

        val nextSchedule = schedules
            .filter { it.timestamp.isAfter(now) }
            .minByOrNull { it.timestamp }

        var summaryText = application.getString(R.string.summary_greeting, dependentName)
        var nextEventTime: LocalDateTime? = null

        if (nextDose != null) {
            nextEventTime = nextDose
            summaryText = application.getString(R.string.summary_next_medication, nextDose.format(timeFormatter))
        }

        if (nextSchedule != null) {
            if (nextEventTime == null || nextSchedule.timestamp.isBefore(nextEventTime)) {
                summaryText = application.getString(R.string.summary_next_appointment, nextSchedule.titulo, nextSchedule.timestamp.format(timeFormatter))
            }
        }
        _summaryText.postValue(summaryText)
    }

    private fun createWeeklySummary(hydrationRecords: List<Hidratacao>, activityRecords: List<AtividadeFisica>) {
        if (hydrationRecords.isEmpty() && activityRecords.isEmpty()) {
            _weeklySummaryState.postValue(null)
            return
        }
        val totalMl = hydrationRecords.sumOf { it.quantidadeMl }
        val mediaDiariaMl = if (hydrationRecords.isNotEmpty()) totalMl.toDouble() / 7.0 else 0.0
        val totalMinutos = activityRecords.sumOf { it.duracaoMinutos }
        _weeklySummaryState.postValue(WeeklySummary(mediaDiariaMl, totalMinutos))
    }

    private fun buildAllCategories(dependente: Dependente) {
        _careManagementCategories.value = buildCareManagementCategories(dependente)
        _healthDataCategories.value = buildHealthDataCategories(dependente)
        _profileManagementCategories.value = buildProfileManagementCategories(dependente)
        _advancedToolsCategories.value = buildAdvancedToolsCategories()
    }

    private fun buildCareManagementCategories(dependente: Dependente): List<DashboardCategory> {
        val list = mutableListOf<DashboardCategory>()
        if (!userPreferences.getIsCaregiver()) {
            list.add(DashboardCategory(application.getString(R.string.category_emergency), R.drawable.ic_sos, ACTION_ID_EMERGENCY))
        }
        list.add(DashboardCategory(application.getString(R.string.category_medications), R.drawable.ic_medicamentoss, R.id.listMedicamentosFragment))
        list.add(DashboardCategory(application.getString(R.string.category_pharmacy_express), R.drawable.ic_local_pharmacy, R.id.action_global_to_pharmacySelectionFragment))
        list.add(DashboardCategory(application.getString(R.string.category_wellbeing_diary), R.drawable.ic_bem_estar, R.id.wellbeingDiaryFragment))
        list.add(DashboardCategory(application.getString(R.string.category_schedule), R.drawable.ic_agenda, R.id.healthScheduleFragment))
        list.add(DashboardCategory(application.getString(R.string.category_notes), R.drawable.ic_anocacoes, R.id.healthNotesFragment))
        list.add(DashboardCategory(application.getString(R.string.category_reminders), R.drawable.ic_alarm, R.id.action_global_to_reminders))
        list.add(DashboardCategory(application.getString(R.string.category_medication_box), R.drawable.ic_farmacinha, R.id.farmacinhaFragment))
        return list.distinctBy { it.actionId }
    }

    private fun buildHealthDataCategories(dependente: Dependente): List<DashboardCategory> {
        val list = mutableListOf<DashboardCategory>()
        list.add(DashboardCategory(application.getString(R.string.category_timeline), R.drawable.ic_timlineic, R.id.timelineFragment))
        list.add(DashboardCategory(application.getString(R.string.category_dose_history), R.drawable.ic_historico_de_doses, R.id.action_global_to_doseHistoryFragment))
        list.add(DashboardCategory(application.getString(R.string.category_documents), R.drawable.ic_documentosg, R.id.healthDocumentsFragment))
        list.add(DashboardCategory(application.getString(R.string.category_education_center), R.drawable.ic_educacao, R.id.action_global_to_educationCenterFragment))
        list.add(DashboardCategory(application.getString(R.string.category_achievements), R.drawable.ic_conquista, R.id.action_global_to_achievementsFragment))
        val age = AgeCalculator.calculateAge(dependente.dataDeNascimento)
        if (age != null && age <= 18) {
            list.add(DashboardCategory(application.getString(R.string.category_vaccines), R.drawable.ic_vacinacao, R.id.action_global_to_vaccinationCardFragment))
        }
        if (age != null && age >= 60) {
            list.add(DashboardCategory(application.getString(R.string.category_geriatric_care), R.drawable.ic_seniorcuidado, R.id.action_global_to_geriatricCareFragment))
        }
        if (dependente.sexo == Sexo.FEMININO.name) {
            list.add(DashboardCategory(application.getString(R.string.category_cycle), R.drawable.ic_menstrual, R.id.action_global_to_cycleTrackerFragment))
        }
        return list.distinctBy { it.actionId }
    }

    private fun buildAdvancedToolsCategories(): List<DashboardCategory> {
        val list = mutableListOf<DashboardCategory>()
        val isCaregiver = userPreferences.getIsCaregiver()
        val isPremium = userPreferences.isPremium()
        if (isCaregiver) {
            if (isPremium) {
                list.add(DashboardCategory(application.getString(R.string.category_chat_ia), R.drawable.ic_conversa, R.id.action_global_to_chatFragment))
                list.add(DashboardCategory(application.getString(R.string.category_predictive_analysis), R.drawable.ic_cerebro, ACTION_ID_SHOW_ANALYSIS_DIALOG))
                list.add(DashboardCategory(application.getString(R.string.category_scan_prescription), R.drawable.ic_scanear, R.id.action_global_to_prescriptionScannerFragment))
                list.add(DashboardCategory(application.getString(R.string.category_reports), R.drawable.ic_relatorios, R.id.reportsFragment))
            } else {
                list.add(DashboardCategory(application.getString(R.string.category_premium_features), R.drawable.ic_premiumc, R.id.action_global_to_premiumPlansFragment))
                list.add(DashboardCategory(application.getString(R.string.category_reports), R.drawable.ic_relatorios, R.id.reportsFragment))
            }
        }
        return list
    }

    private fun buildProfileManagementCategories(dependente: Dependente): List<DashboardCategory> {
        val list = mutableListOf<DashboardCategory>()
        if (userPreferences.getIsCaregiver()) {
            list.add(DashboardCategory(application.getString(R.string.category_goals), R.drawable.ic_metas, R.id.action_global_to_healthGoalsFragment))
            list.add(DashboardCategory(application.getString(R.string.category_edit_profile), R.drawable.ic_editarperfil, R.id.action_global_to_addEditDependentFragment))
            list.add(DashboardCategory(application.getString(R.string.category_caregivers), R.drawable.ic_convdependente, R.id.action_global_to_manageCaregiversFragment))
            list.add(DashboardCategory(application.getString(R.string.category_credentials), R.drawable.ic_credenciais, ACTION_ID_VIEW_CREDENTIALS))
            list.add(DashboardCategory(application.getString(R.string.category_manage_archived), R.drawable.ic_gerenciar, R.id.action_global_to_archivedMedicationsFragment))
            if (!dependente.isSelfCareProfile) {
                list.add(DashboardCategory(application.getString(R.string.category_delete_profile), R.drawable.ic_delete_red, ACTION_ID_DELETE_DEPENDENT))
            }
        }
        return list
    }

    fun saveAnalysisToHistory(prompt: String, analysisResult: String) {
        viewModelScope.launch {
            val currentDependentId = _dependentId.value ?: return@launch
            val currentUserId = userRepository.getCurrentUser()?.uid ?: "caregiver_user"
            val historyEntry = AnalysisHistory(
                dependentId = currentDependentId,
                userId = currentUserId,
                symptomsPrompt = prompt,
                analysisResult = analysisResult
            )
            val result = firestoreRepository.saveAnalysisHistory(historyEntry)
            if (result.isFailure) {
                _actionFeedback.postValue(Event(application.getString(R.string.error_analysis_history_save)))
            }
        }
    }

    fun onViewCredentialsClicked() {
        dependente.value?.let { _showCredentials.value = Event(Pair(it.codigoDeVinculo, it.senha)) }
    }

    @SuppressLint("StringFormatMatches")
    fun onToggleAlarmClicked() {
        dependente.value?.let {
            val newStatus = !it.usaAlarmeTelaCheia
            viewModelScope.launch {
                firestoreRepository.updateUsaAlarmeTelaCheia(it.id, newStatus).onSuccess {
                    val statusText = if (newStatus) application.getString(R.string.status_activated) else application.getString(R.string.status_deactivated)
                    _actionFeedback.postValue(Event(application.getString(R.string.fullscreen_alarm_status_updated, statusText)))
                }
            }
        }
    }

    fun onDeleteDependentClicked() {
        if (dependente.value?.isSelfCareProfile == true) {
            _actionFeedback.value = Event(application.getString(R.string.error_cannot_delete_self_profile_here))
        } else {
            _showDeleteConfirmation.value = Event(Unit)
        }
    }

    fun confirmDeleteDependent() {
        dependente.value?.id?.let { dependentId ->
            viewModelScope.launch {
                firestoreRepository.deleteDependentAndAllData(dependentId).onSuccess {
                    _actionFeedback.postValue(Event(application.getString(R.string.dependent_deleted_success)))
                }.onFailure {
                    _actionFeedback.postValue(Event(application.getString(R.string.dependent_deleted_error, it.message)))
                }
            }
        }
    }

    fun onEmergencyClicked() {
        _showEmergencyConfirmation.postValue(Event(Unit))
    }

    fun confirmEmergencyAlert() {
        viewModelScope.launch {
            val currentDependentId = _dependentId.value ?: return@launch
            firestoreRepository.triggerEmergencyAlert(currentDependentId).onSuccess {
                _actionFeedback.postValue(Event(application.getString(R.string.emergency_alert_sent_success)))
                saveLog(currentDependentId, application.getString(R.string.log_emergency_button_pressed), TipoAtividade.ANOTACAO_CRIADA)
            }.onFailure {
                _actionFeedback.postValue(Event(application.getString(R.string.emergency_alert_sent_fail)))
            }
        }
    }

    suspend fun getPredictiveAnalysis(
        symptoms: String, startDate: LocalDate, endDate: LocalDate,
        includeDoseHistory: Boolean, includeHealthNotes: Boolean, includeContinuousMeds: Boolean
    ): Result<String> {
        _showLoading.postValue(Event(application.getString(R.string.analysis_loading_message)))
        try {
            val currentDependentId = _dependentId.value ?: return Result.failure(Exception(application.getString(R.string.error_dependent_id_not_found)))
            return firestoreRepository.getPredictiveAnalysis(
                currentDependentId, symptoms, startDate, endDate,
                includeDoseHistory, includeHealthNotes, includeContinuousMeds, dependente.value
            )
        } finally {
            _hideLoading.postValue(Event(Unit))
        }
    }

    private fun calculateImc(weightStr: String, heightStr: String) {
        val weight = weightStr.replace(',', '.').toFloatOrNull()
        val height = heightStr.replace(',', '.').toFloatOrNull()
        if (weight != null && height != null && height > 0) {
            val heightInMeters = height / 100
            val imc = weight / (heightInMeters * heightInMeters)
            val (classification, color) = when {
                imc < 18.5 -> application.getString(R.string.imc_classification_underweight) to R.color.warning_orange
                imc < 25 -> application.getString(R.string.imc_classification_normal) to R.color.success_green
                imc < 30 -> application.getString(R.string.imc_classification_overweight) to R.color.warning_orange
                else -> application.getString(R.string.imc_classification_obesity) to R.color.error_red
            }
            _imcResult.postValue(ImcResult(imc, classification, color))
        } else {
            _imcResult.postValue(null)
        }
    }

    private fun calculateWeightGoalStatus(dependente: Dependente, healthNotes: List<HealthNote>) {
        val currentWeight = dependente.peso.replace(',', '.').toFloatOrNull()
        val targetWeight = dependente.pesoMeta.replace(',', '.').toFloatOrNull()
        if (currentWeight == null || targetWeight == null || targetWeight <= 0) {
            _weightGoalStatus.postValue(null)
            return
        }
        val initialWeightRecord = healthNotes
            .filter { it.type == HealthNoteType.WEIGHT && it.values["weight"]?.isNotBlank() == true }
            .minByOrNull { it.timestamp }
        val initialWeight = initialWeightRecord?.values?.get("weight")?.replace(',', '.')?.toFloatOrNull()
        if (initialWeight == null) {
            _weightGoalStatus.postValue(null)
            return
        }
        val totalDistance = abs(initialWeight - targetWeight)
        val distanceCovered = abs(initialWeight - currentWeight)
        val progress = if (totalDistance > 0.1f) ((distanceCovered / totalDistance) * 100).toInt().coerceIn(0, 100) else 100
        val difference = currentWeight - targetWeight
        val (progressText, color) = when {
            abs(difference) <= 0.5 -> application.getString(R.string.goal_achieved) to R.color.success_green
            currentWeight > targetWeight -> application.getString(R.string.goal_to_go_format, difference) to R.color.md_theme_primary
            else -> application.getString(R.string.goal_to_go_format, abs(difference)) to R.color.md_theme_secondary
        }
        _weightGoalStatus.postValue(WeightGoalStatus(progress, progressText, color))
    }

    private suspend fun saveLog(dependentId: String, descricao: String, tipo: TipoAtividade) {
        val autorNome = userPreferences.getUserName()
        val autorId = if (userPreferences.getIsCaregiver()) userRepository.getCurrentUser()?.uid ?: "" else "dependent_user"
        val log = Atividade(
            descricao = "$autorNome $descricao",
            tipo = tipo,
            autorId = autorId,
            autorNome = autorNome
        )
        firestoreRepository.saveActivityLog(dependentId, log)
    }
}