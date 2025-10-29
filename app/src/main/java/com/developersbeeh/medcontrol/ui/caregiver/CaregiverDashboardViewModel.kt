package com.developersbeeh.medcontrol.ui.caregiver

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.data.repository.*
import com.developersbeeh.medcontrol.ui.dashboard.WeightGoalStatus
import com.developersbeeh.medcontrol.util.AgeCalculator
import com.developersbeeh.medcontrol.util.DoseTimeCalculator
import com.developersbeeh.medcontrol.util.Event
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "CaregiverDashboardVM"

enum class ProximaDoseStatus {
    ATRASADA, EM_DIA, CONCLUIDA, NENHUMA
}

data class DependentWithStatus(
    val dependente: Dependente,
    val age: Int?,
    val proximaDoseTexto: String,
    val proximaDoseStatus: ProximaDoseStatus,
    val dosesTomadasHoje: Int,
    val dosesEsperadasHoje: Int,
    val aderencia7dias: Int,
    val hasLowStock: Boolean,
    val hasAppointmentToday: Boolean,
    val unreadInsightsCount: Int,
    val latestUnreadInsightPreview: String?,
    val adherenceStreak: Int,
    val missedDoseMedicationNames: List<String>,
    val weightGoalStatus: WeightGoalStatus?
)

data class NewDependentInfo(val name: String, val code: String, val password: String)

data class DashboardSummary(
    val totalDependents: Int = 0,
    val totalDosesHoje: Int = 0,
    val dosesTomadasHoje: Int = 0,
    val dosesAtrasadasHoje: Int = 0,
    val proximaDoseGeral: String = "Nenhuma dose futura",
    val compromissosHoje: Int = 0
)

@HiltViewModel
class CaregiverDashboardViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository,
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _dependentsWithStatus = MutableLiveData<UiState<List<DependentWithStatus>>>()
    val dependentsWithStatus: LiveData<UiState<List<DependentWithStatus>>> = _dependentsWithStatus

    private val _newDependentCreatedEvent = MutableLiveData<Event<NewDependentInfo>>()
    val newDependentCreatedEvent: LiveData<Event<NewDependentInfo>> = _newDependentCreatedEvent

    private val _dashboardSummary = MutableLiveData<DashboardSummary>()
    val dashboardSummary: LiveData<DashboardSummary> = _dashboardSummary

    private val _pendingInvites = MutableLiveData<List<Convite>>()
    val pendingInvites: LiveData<List<Convite>> = _pendingInvites

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    private val _navigateToDependentDashboard = MutableLiveData<Event<Pair<DependentWithStatus, View>>>()
    val navigateToDependentDashboard: LiveData<Event<Pair<DependentWithStatus, View>>> = _navigateToDependentDashboard


    init {
        loadDashboardData()
    }

    fun forceReload() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _dependentsWithStatus.value = UiState.Loading
        viewModelScope.launch {
            try {
                // 1. Busca os convites
                val email = userRepository.getCurrentUser()?.email ?: ""
                if (email.isNotEmpty()) {
                    val invites = userRepository.getReceivedInvites(email).first()
                    _pendingInvites.postValue(invites)
                }

                // 2. Escuta a lista de dependentes em tempo real
                firestoreRepository.getDependentes().collect { dependents ->
                    if (dependents.isEmpty()) {
                        _dependentsWithStatus.postValue(UiState.Success(emptyList()))
                        _dashboardSummary.postValue(DashboardSummary())
                        return@collect
                    }

                    val allSchedulesForAllDependents = mutableListOf<AgendamentoSaude>()

                    // 3. Processa cada dependente individualmente
                    val dependentsWithStatusList = dependents.map { dependent ->
                        // ✅ CORREÇÃO: Filtra medicamentos arquivados antes de qualquer cálculo
                        val medicamentos = medicationRepository.getMedicamentos(dependent.id).first()
                            .filter { !it.isArchived }

                        val doses = medicationRepository.getDoseHistory(dependent.id).first()
                        val schedules = scheduleRepository.getSchedules(dependent.id).first()
                        val insights = firestoreRepository.getInsights(dependent.id).first()
                        val healthNotes = firestoreRepository.getHealthNotes(dependent.id).first()

                        allSchedulesForAllDependents.addAll(schedules)

                        val (texto, status) = DoseTimeCalculator.calcularProximaDoseGeral(medicamentos, doses)
                        val (tomadasHoje, esperadasHoje) = DoseTimeCalculator.calcularDosesDeHoje(medicamentos, doses)
                        val aderencia7d = DoseTimeCalculator.calcularAderencia7dias(medicamentos, doses)
                        val hasLowStock = medicamentos.any { it.estoqueAtualTotal <= it.nivelDeAlertaEstoque && it.nivelDeAlertaEstoque > 0 }
                        val hasAppointmentToday = schedules.any { it.timestamp.toLocalDate() == LocalDate.now() }
                        val unreadInsights = insights.filter { !it.isRead }
                        val streak = userPreferences.getAdherenceStreak(dependent.id)
                        val age = AgeCalculator.calculateAge(dependent.dataDeNascimento)
                        val missedDoseMeds = if (status == ProximaDoseStatus.ATRASADA) DoseTimeCalculator.getMissedDoseMedications(medicamentos, doses) else emptyList()
                        val weightGoalStatus = calculateWeightGoalStatus(dependent, healthNotes)

                        DependentWithStatus(
                            dependente = dependent,
                            age = age,
                            proximaDoseTexto = texto,
                            proximaDoseStatus = status,
                            dosesTomadasHoje = tomadasHoje,
                            dosesEsperadasHoje = esperadasHoje,
                            aderencia7dias = aderencia7d,
                            hasLowStock = hasLowStock,
                            hasAppointmentToday = hasAppointmentToday,
                            unreadInsightsCount = unreadInsights.size,
                            latestUnreadInsightPreview = unreadInsights.maxByOrNull { it.timestamp?.toDate()?.time ?: 0 }?.description,
                            missedDoseMedicationNames = missedDoseMeds.map { it.nome },
                            adherenceStreak = streak,
                            weightGoalStatus = weightGoalStatus
                        )
                    }

                    // Calcula o resumo GERAL com os dados de todos os dependentes
                    calculateDashboardSummary(dependentsWithStatusList, allSchedulesForAllDependents)
                    _dependentsWithStatus.postValue(UiState.Success(dependentsWithStatusList.sortedBy { it.dependente.nome }))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao carregar dados do painel: ", e)
                _dependentsWithStatus.postValue(UiState.Error("Não foi possível carregar os dependentes."))
            }
        }
    }

    private fun calculateDashboardSummary(packages: List<DependentWithStatus>, allSchedules: List<AgendamentoSaude>) {
        viewModelScope.launch(Dispatchers.Default) {
            val totalDosesHoje = packages.sumOf { it.dosesEsperadasHoje }
            val dosesTomadasHoje = packages.sumOf { it.dosesTomadasHoje }
            val dosesAtrasadasHoje = packages.sumOf { it.missedDoseMedicationNames.size }

            val proximaDoseGeralTexto = packages
                .filter { it.proximaDoseStatus == ProximaDoseStatus.EM_DIA && it.proximaDoseTexto.contains("Hoje") }
                .minByOrNull { it.proximaDoseTexto }?.proximaDoseTexto ?: "Nenhuma dose futura"

            val compromissosHoje = allSchedules.count { it.timestamp.toLocalDate() == LocalDate.now() }

            _dashboardSummary.postValue(
                DashboardSummary(
                    totalDependents = packages.size,
                    totalDosesHoje = totalDosesHoje,
                    dosesTomadasHoje = dosesTomadasHoje,
                    dosesAtrasadasHoje = dosesAtrasadasHoje,
                    proximaDoseGeral = proximaDoseGeralTexto,
                    compromissosHoje = compromissosHoje
                )
            )
        }
    }

    fun onDependentSelected(dependentWithStatus: DependentWithStatus, view: View) {
        _navigateToDependentDashboard.value = Event(dependentWithStatus to view)
    }

    fun postFeedbackMessage(message: String) {
        _actionFeedback.postValue(Event(message))
    }

    private fun calculateWeightGoalStatus(dependente: Dependente, healthNotes: List<HealthNote>): WeightGoalStatus? {
        val currentWeight = dependente.peso.replace(',', '.').toFloatOrNull()
        val targetWeight = dependente.pesoMeta.replace(',', '.').toFloatOrNull()
        if (currentWeight == null || targetWeight == null || targetWeight <= 0) return null

        val initialWeightRecord = healthNotes
            .filter { it.type == HealthNoteType.WEIGHT && it.values["weight"]?.isNotBlank() == true }
            .minByOrNull { it.timestamp }
        val initialWeight = initialWeightRecord?.values?.get("weight")?.replace(',', '.')?.toFloatOrNull() ?: currentWeight

        val totalDistance = abs(initialWeight - targetWeight)
        val distanceCovered = abs(initialWeight - currentWeight)
        val progress = if (totalDistance > 0.1f) ((distanceCovered / totalDistance) * 100).toInt().coerceIn(0, 100) else 100
        val difference = currentWeight - targetWeight

        val (progressText, color) = when {
            abs(difference) <= 0.5 -> "Meta atingida!" to R.color.success_green
            currentWeight > targetWeight -> "Faltam ${String.format(Locale.getDefault(), "%.1f", difference)} kg" to R.color.md_theme_primary
            else -> "Faltam ${String.format(Locale.getDefault(), "%.1f", abs(difference))} kg" to R.color.md_theme_secondary
        }
        return WeightGoalStatus(progress, progressText, color)
    }

    fun acceptInvite(invite: Convite) {
        viewModelScope.launch {
            userRepository.acceptInvite(invite).onSuccess {
                postFeedbackMessage("Convite aceito! O novo dependente aparecerá na sua lista.")
                forceReload()
            }.onFailure {
                postFeedbackMessage("Erro ao aceitar convite: ${it.message}")
            }
        }
    }

    fun declineInvite(invite: Convite) {
        viewModelScope.launch {
            userRepository.cancelInvite(invite.id).onSuccess {
                postFeedbackMessage("Convite recusado.")
                forceReload()
            }.onFailure {
                postFeedbackMessage("Erro ao recusar o convite.")
            }
        }
    }

    fun dismissInviteFromUI(invite: Convite) {
        val currentList = _pendingInvites.value?.toMutableList() ?: mutableListOf()
        currentList.remove(invite)
        _pendingInvites.postValue(currentList)
    }

    fun deleteDependent(dependente: Dependente) {
        viewModelScope.launch {
            if (dependente.isSelfCareProfile) {
                postFeedbackMessage("Não é possível excluir seu próprio perfil de autocuidado.")
                return@launch
            }
            val result = firestoreRepository.deleteDependentAndAllData(dependente.id)
            result.onSuccess {
                postFeedbackMessage("${dependente.nome} foi excluído(a).")
            }.onFailure {
                postFeedbackMessage("Erro ao excluir ${dependente.nome}.")
            }
        }
    }

    fun toggleAlarmeTelaCheia(dependente: Dependente) {
        viewModelScope.launch {
            val novoEstado = !dependente.usaAlarmeTelaCheia
            val result = firestoreRepository.updateUsaAlarmeTelaCheia(dependente.id, novoEstado)
            result.onSuccess {
                val status = if (novoEstado) "ativado" else "desativado"
                postFeedbackMessage("Alarme de tela cheia $status para ${dependente.nome}.")
            }
        }
    }
}