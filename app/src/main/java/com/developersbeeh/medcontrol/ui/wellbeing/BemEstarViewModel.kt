package com.developersbeeh.medcontrol.ui.wellbeing

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MealAnalysisRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
// ✅ IMPORT ADICIONADO: Importa a função de extensão do pacote 'meals'
import com.developersbeeh.medcontrol.ui.meals.getDisplayName

data class BemEstarUiState(
    val dependente: Dependente? = null,
    val hidratacaoTotalMl: Int = 0,
    val hidratacaoMetaMl: Int = 2000,
    val hidratacaoPorcentagem: Int = 0,
    val atividadeTotalMin: Int = 0,
    val atividadeMetaMin: Int = 30,
    val atividadePorcentagem: Int = 0,
    val refeicoesRegistradas: Int = 0,
    val caloriasTotal: Int = 0,
    val caloriasMeta: Int = 2000,
    val caloriasPorcentagem: Int = 0,
    val registroSono: RegistroSono? = null,
    val sonoTotalHoras: Long = 0,
    val sonoTotalMinutos: Long = 0,
    val imcResult: ImcResult? = null,
    val pesoMetaStatus: WeightGoalStatus? = null
)

data class ImcResult(val value: Float, val classification: String, val color: Int)
data class WeightGoalStatus(val progress: Int, val progressText: String, val color: Int)

sealed class MealAnalysisState {
    object Idle : MealAnalysisState()
    object Loading : MealAnalysisState()
    data class Success(val result: MealAnalysisResult) : MealAnalysisState()
    data class Error(val message: String) : MealAnalysisState()
}

private data class InitialWellbeingData(
    val dependent: Dependente?,
    val hydrationRecords: List<Hidratacao>,
    val activityRecords: List<AtividadeFisica>,
    val mealRecords: List<Refeicao>,
    val sleepRecords: List<RegistroSono>
)

@HiltViewModel
class BemEstarViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val mealAnalysisRepository: MealAnalysisRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val _dependentId = MutableLiveData<String>()

    private val _uiState = MutableLiveData<BemEstarUiState>()
    val uiState: LiveData<BemEstarUiState> = _uiState

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    private val _mealAnalysisState = MutableLiveData<MealAnalysisState>(MealAnalysisState.Idle)
    val mealAnalysisState: LiveData<MealAnalysisState> = _mealAnalysisState

    private val _sleepSuggestionEvent = MutableLiveData<Event<Pair<LocalTime, LocalTime>>>()
    val sleepSuggestionEvent: LiveData<Event<Pair<LocalTime, LocalTime>>> = _sleepSuggestionEvent

    fun initialize(dependentId: String) {
        if (_dependentId.value == dependentId) return
        _dependentId.value = dependentId

        viewModelScope.launch {
            val today = LocalDate.now()
            combine(
                firestoreRepository.listenToDependentProfile(dependentId),
                firestoreRepository.getHidratacaoHistory(dependentId, today),
                firestoreRepository.getAtividadeFisicaHistory(dependentId, today),
                firestoreRepository.getRefeicoesHistory(dependentId, today),
                firestoreRepository.getSonoHistory(dependentId, today.minusDays(1), today)
            ) { dependent, hydration, activity, meals, sleep ->
                InitialWellbeingData(dependent, hydration, activity, meals, sleep)
            }.combine(firestoreRepository.getHealthNotes(dependentId)) { initialData, healthNotes ->
                processAllData(
                    initialData.dependent,
                    initialData.hydrationRecords,
                    initialData.activityRecords,
                    initialData.mealRecords,
                    initialData.sleepRecords,
                    healthNotes
                )
            }.collectLatest { }
        }
    }

    private fun processAllData(
        dependent: Dependente?,
        hydrationRecords: List<Hidratacao>,
        activityRecords: List<AtividadeFisica>,
        mealRecords: List<Refeicao>,
        sleepRecords: List<RegistroSono>,
        healthNotes: List<HealthNote>
    ) {
        if (dependent == null) return

        val hidratacaoTotal = hydrationRecords.sumOf { it.quantidadeMl }
        val hidratacaoMeta = dependent.metaHidratacaoMl
        val hidratacaoPorcentagem = if (hidratacaoMeta > 0) ((hidratacaoTotal.toDouble() / hidratacaoMeta) * 100).toInt() else 0

        val atividadeTotal = activityRecords.sumOf { it.duracaoMinutos }
        val atividadeMeta = dependent.metaAtividadeMinutos
        val atividadePorcentagem = if (atividadeMeta > 0) ((atividadeTotal.toDouble() / atividadeMeta) * 100).toInt() else 0

        val caloriasTotal = mealRecords.sumOf { it.calorias ?: 0 }
        val caloriasMeta = dependent.metaCaloriasDiarias
        val caloriasPorcentagem = if (caloriasMeta > 0) ((caloriasTotal.toDouble() / caloriasMeta) * 100).toInt() else 0

        val registroSonoHoje = sleepRecords.firstOrNull { it.getDataAsLocalDate().isEqual(LocalDate.now()) }
        var sonoHoras = 0L
        var sonoMinutos = 0L
        if (registroSonoHoje != null) {
            val start = registroSonoHoje.getHoraDeDormirAsLocalTime()
            val end = registroSonoHoje.getHoraDeAcordarAsLocalTime()
            val duration = Duration.between(start, end)
            val totalDuration = if (duration.isNegative) duration.plusDays(1) else duration
            sonoHoras = totalDuration.toHours()
            sonoMinutos = totalDuration.toMinutes() % 60
        }

        val imcResult = calculateImc(dependent.peso, dependent.altura)
        val pesoMetaStatus = calculateWeightGoalStatus(dependent, healthNotes)

        _uiState.postValue(BemEstarUiState(
            dependente = dependent,
            hidratacaoTotalMl = hidratacaoTotal,
            hidratacaoMetaMl = hidratacaoMeta,
            hidratacaoPorcentagem = hidratacaoPorcentagem.coerceAtMost(100),
            atividadeTotalMin = atividadeTotal,
            atividadeMetaMin = atividadeMeta,
            atividadePorcentagem = atividadePorcentagem.coerceAtMost(100),
            refeicoesRegistradas = mealRecords.size,
            caloriasTotal = caloriasTotal,
            caloriasMeta = caloriasMeta,
            caloriasPorcentagem = caloriasPorcentagem.coerceAtMost(100),
            registroSono = registroSonoHoje, // Armazena apenas o registro de hoje
            sonoTotalHoras = sonoHoras,
            sonoTotalMinutos = sonoMinutos,
            imcResult = imcResult,
            pesoMetaStatus = pesoMetaStatus
        ))
    }

    private fun calculateImc(weightStr: String, heightStr: String): ImcResult? {
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
            return ImcResult(imc, classification, color)
        }
        return null
    }

    private fun calculateWeightGoalStatus(dependente: Dependente, healthNotes: List<HealthNote>): WeightGoalStatus? {
        val currentWeight = dependente.peso.replace(',', '.').toFloatOrNull()
        val targetWeight = dependente.pesoMeta.replace(',', '.').toFloatOrNull()

        if (currentWeight == null || targetWeight == null || targetWeight <= 0) {
            return null
        }

        val initialWeightRecord = healthNotes
            .filter { it.type == HealthNoteType.WEIGHT && it.values["weight"]?.isNotBlank() == true }
            .minByOrNull { it.timestamp }
        val initialWeight = initialWeightRecord?.values?.get("weight")?.replace(',', '.')?.toFloatOrNull() ?: currentWeight

        val totalDistance = abs(initialWeight - targetWeight)
        val distanceCovered = abs(initialWeight - currentWeight)

        val progress = if (totalDistance > 0.1f) {
            ((distanceCovered / totalDistance) * 100).toInt().coerceIn(0, 100)
        } else { 100 }

        val difference = currentWeight - targetWeight
        val progressText: String
        val color: Int

        when {
            abs(difference) <= 0.5 -> {
                progressText = application.getString(R.string.goal_achieved_congrats)
                color = R.color.success_green
            }
            currentWeight > targetWeight -> {
                progressText = application.getString(R.string.goal_to_go_format_kg, String.format(Locale.getDefault(), "%.1f", difference), targetWeight.toString())
                color = R.color.md_theme_primary
            }
            else -> {
                progressText = application.getString(R.string.goal_to_go_format_kg, String.format(Locale.getDefault(), "%.1f", abs(difference)), targetWeight.toString())
                color = R.color.md_theme_secondary
            }
        }
        return WeightGoalStatus(progress, progressText, color)
    }

    fun updateWeight(newWeight: String) {
        viewModelScope.launch {
            val dependentId = _dependentId.value ?: return@launch
            if (newWeight.isBlank()) {
                _actionFeedback.postValue(Event(application.getString(R.string.error_invalid_weight_value)))
                return@launch
            }
            val note = HealthNote(
                type = HealthNoteType.WEIGHT,
                values = mapOf("weight" to newWeight)
            )
            firestoreRepository.saveHealthNote(dependentId, note)
            val result = firestoreRepository.updateDependentWeight(dependentId, newWeight)
            if(result.isSuccess) {
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_weight_updated)))
                saveLog(dependentId, application.getString(R.string.log_weight_updated, newWeight), TipoAtividade.ANOTACAO_CRIADA)
            } else {
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_weight_update_fail)))
            }
        }
    }

    fun addWaterIntake(quantidadeMl: Int) {
        viewModelScope.launch {
            val dependentId = _dependentId.value ?: return@launch
            val novoRegistro = Hidratacao(quantidadeMl = quantidadeMl)
            val result = firestoreRepository.saveHidratacaoRecord(dependentId, novoRegistro)
            if (result.isSuccess) {
                saveLog(dependentId, application.getString(R.string.log_water_intake, quantidadeMl), TipoAtividade.ANOTACAO_CRIADA)
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_water_logged, quantidadeMl)))
            } else {
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_water_log_fail)))
            }
        }
    }

    fun addAtividadeFisica(tipo: String, duracao: Int) {
        viewModelScope.launch {
            val dependentId = _dependentId.value ?: return@launch
            if (tipo.isBlank() || duracao <= 0) {
                _actionFeedback.postValue(Event(application.getString(R.string.error_activity_duration_required)))
                return@launch
            }
            val novoRegistro = AtividadeFisica(tipo = tipo, duracaoMinutos = duracao)
            val result = firestoreRepository.saveAtividadeFisicaRecord(dependentId, novoRegistro)
            if (result.isSuccess) {
                saveLog(dependentId, application.getString(R.string.log_activity_logged, duracao, tipo), TipoAtividade.ANOTACAO_CRIADA)
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_activity_logged)))
            } else {
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_activity_log_fail)))
            }
        }
    }

    // ✅ CORREÇÃO: Função atualizada para receber 'aiResult'
    fun addRefeicao(tipo: TipoRefeicao, descricao: String, calorias: Int?, aiResult: MealAnalysisResult?) {
        viewModelScope.launch {
            val dependentId = _dependentId.value ?: return@launch
            if (descricao.isBlank()) {
                _actionFeedback.postValue(Event(application.getString(R.string.error_meal_description_required)))
                return@launch
            }

            val caloriasFinais = calorias ?: aiResult?.calorias

            val novoRegistro = Refeicao(tipo = tipo.name, descricao = descricao, calorias = caloriasFinais)
            val result = firestoreRepository.saveRefeicaoRecord(dependentId, novoRegistro)
            if (result.isSuccess) {
                // ✅ CORREÇÃO: 'getDisplayName' agora é encontrada
                val logDesc = caloriasFinais?.let {
                    application.getString(R.string.log_meal_logged_kcal, tipo.getDisplayName(application), it)
                } ?: application.getString(R.string.log_meal_logged, tipo.getDisplayName(application))

                saveLog(dependentId, logDesc, TipoAtividade.ANOTACAO_CRIADA)
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_meal_logged)))
            } else {
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_meal_log_fail)))
            }
        }
    }

    fun saveSono(data: LocalDate, horaDormir: LocalTime, horaAcordar: LocalTime, qualidade: QualidadeSono, notas: String?, interrupcoes: Int) {
        viewModelScope.launch {
            val dependentId = _dependentId.value ?: return@launch
            val registroId = uiState.value?.registroSono?.id ?: UUID.randomUUID().toString()
            val registro = RegistroSono(
                id = registroId,
                data = data.format(DateTimeFormatter.ISO_LOCAL_DATE),
                horaDeDormir = horaDormir.format(DateTimeFormatter.ISO_LOCAL_TIME),
                horaDeAcordar = horaAcordar.format(DateTimeFormatter.ISO_LOCAL_TIME),
                qualidade = qualidade.name,
                notas = notas,
                interrupcoes = interrupcoes
            )
            val result = firestoreRepository.saveSonoRecord(dependentId, registro)
            if (result.isSuccess) {
                val duracao = Duration.between(horaDormir, horaAcordar)
                val totalHoras = if(duracao.isNegative) duracao.plusDays(1).toHours() else duracao.toHours()
                saveLog(dependentId, application.getString(R.string.log_sleep_logged, totalHoras, interrupcoes), TipoAtividade.ANOTACAO_CRIADA)
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_sleep_logged)))
            } else {
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_sleep_log_fail)))
            }
        }
    }

    fun requestSleepTimeSuggestion() {
        viewModelScope.launch {
            val dependentId = _dependentId.value ?: return@launch
            val today = LocalDate.now()
            val sevenDaysAgo = today.minusDays(7)

            val records = firestoreRepository.getSonoHistory(dependentId, sevenDaysAgo, today).first()
            if (records.size < 3) {
                _actionFeedback.postValue(Event(application.getString(R.string.feedback_sleep_suggestion_unavailable)))
                return@launch
            }

            var totalBedtimeMinutes = 0.0
            var totalWaketimeMinutes = 0.0
            var bedtimeCount = 0
            var waketimeCount = 0

            records.forEach { record ->
                val bedtime = record.getHoraDeDormirAsLocalTime()
                val waketime = record.getHoraDeAcordarAsLocalTime()

                val bedtimeInMinutes = bedtime.hour * 60 + bedtime.minute
                totalBedtimeMinutes += if (bedtime.hour > 12) (bedtimeInMinutes - 1440) else bedtimeInMinutes
                bedtimeCount++

                totalWaketimeMinutes += (waketime.hour * 60 + waketime.minute)
                waketimeCount++
            }

            val avgBedtimeRawMinutes = totalBedtimeMinutes / bedtimeCount
            val avgWaketimeRawMinutes = totalWaketimeMinutes / waketimeCount
            val avgBedtimeMinutes = (avgBedtimeRawMinutes + 1440) % 1440
            val avgWaketimeMinutes = avgWaketimeRawMinutes

            val suggestedBedtime = LocalTime.of((avgBedtimeMinutes / 60).toInt(), (avgBedtimeMinutes % 60).toInt())
            val suggestedWaketime = LocalTime.of((avgWaketimeMinutes / 60).toInt(), (avgWaketimeMinutes % 60).toInt())

            _sleepSuggestionEvent.postValue(Event(Pair(suggestedBedtime, suggestedWaketime)))
        }
    }

    fun analyzeMealPhoto(imageUri: Uri) {
        val dependentId = _dependentId.value ?: return
        _mealAnalysisState.value = MealAnalysisState.Loading
        viewModelScope.launch {
            val result = mealAnalysisRepository.analyzeMealPhoto(dependentId, imageUri)
            result.onSuccess {
                _mealAnalysisState.postValue(MealAnalysisState.Success(it))
            }.onFailure {
                _mealAnalysisState.postValue(MealAnalysisState.Error(it.message ?: application.getString(R.string.error_meal_analysis_unknown)))
            }
        }
    }

    fun resetMealAnalysisState() {
        _mealAnalysisState.value = MealAnalysisState.Idle
    }

    /**
     * Limpa o registro de sono atual da UI para permitir um novo registro.
     */
    fun clearCurrentSleepRecordForNewEntry() {
        _uiState.value = _uiState.value?.copy(registroSono = null)
    }

    private suspend fun saveLog(dependentId: String, descricao: String, tipo: TipoAtividade) {
        val autorNome = userPreferences.getUserName()
        val autorId = if (userPreferences.getIsCaregiver()) {
            userRepository.getCurrentUser()?.uid ?: ""
        } else { "dependent_user" }

        val log = Atividade(
            descricao = descricao, // Descrição limpa (sem autor)
            tipo = tipo,
            autorId = autorId,
            autorNome = autorNome // O backend vai usar este campo
        )
        firestoreRepository.saveActivityLog(dependentId, log)
    }
}