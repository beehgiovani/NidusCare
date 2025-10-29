package com.developersbeeh.medcontrol.ui.addmedicamento

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.repository.MedicationRepositoryEnhanced
import com.developersbeeh.medcontrol.ui.common.UiState
import com.developersbeeh.medcontrol.ui.common.launchWithResultState
import com.developersbeeh.medcontrol.ui.common.launchWithState
import com.developersbeeh.medcontrol.ui.common.setError
import com.developersbeeh.medcontrol.ui.common.setIdle
import com.developersbeeh.medcontrol.ui.common.setSuccess
import com.developersbeeh.medcontrol.validation.ValidationResult
import com.developersbeeh.medcontrol.validation.ValidationError
import com.developersbeeh.medcontrol.validation.Severity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AddMedicamentoVMEnhanced"

/**
 * ViewModel melhorado para adicionar/editar medicamentos com validações e UiState.
 */
@HiltViewModel
class AddMedicamentoViewModelEnhanced @Inject constructor(
    private val repository: MedicationRepositoryEnhanced,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Estados
    private val _saveState = MutableLiveData<UiState<Unit>>()
    val saveState: LiveData<UiState<Unit>> = _saveState

    private val _duplicatesState = MutableLiveData<UiState<List<Medicamento>>>()
    val duplicatesState: LiveData<UiState<List<Medicamento>>> = _duplicatesState

    private val _validationState = MutableLiveData<ValidationResult>()
    val validationState: LiveData<ValidationResult> = _validationState

    private val _wizardState = MutableLiveData(WizardState())
    val wizardState: LiveData<WizardState> = _wizardState

    // Medicamento atual
    private val _currentMedicamento = MutableLiveData<Medicamento>()
    val currentMedicamento: LiveData<Medicamento> = _currentMedicamento

    private var currentDependentId: String = ""
    private var isEditMode: Boolean = false

    /**
     * Inicializa o ViewModel para criar ou editar um medicamento.
     */
    fun initialize(dependentId: String, medicamento: Medicamento?) {
        currentDependentId = dependentId
        isEditMode = medicamento != null

        if (medicamento != null) {
            _currentMedicamento.value = medicamento
            updateWizardFromMedicamento(medicamento)
        } else {
            // Verificar se há rascunho salvo
            val draft = userPreferences.getMedicationDraft()
            if (draft != null) {
                _currentMedicamento.value = draft
                updateWizardFromMedicamento(draft)
            } else {
                _currentMedicamento.value = Medicamento(isUsoContinuo = true)
            }
        }

        _saveState.setIdle()
    }

    /**
     * Atualiza o medicamento atual.
     */
    fun updateMedicamento(medicamento: Medicamento) {
        _currentMedicamento.value = medicamento
        
        // Salvar rascunho automaticamente se não estiver em modo de edição
        if (!isEditMode) {
            saveDraft(medicamento)
        }
    }

    /**
     * Valida o medicamento atual.
     */
    fun validate(): ValidationResult {
        val medicamento = _currentMedicamento.value ?: return ValidationResult.Error(
            listOf(ValidationError("medicamento", "Medicamento não inicializado", Severity.CRITICAL))
        )

        val result = repository.validateMedicamento(medicamento)
        _validationState.value = result
        return result
    }

    /**
     * Verifica duplicatas antes de salvar.
     */
    fun checkDuplicates() {
        val medicamento = _currentMedicamento.value ?: return

        launchWithState(
            liveData = _duplicatesState,
            emptyMessage = "Nenhum medicamento similar encontrado"
        ) {
            repository.checkDuplicates(
                dependentId = currentDependentId,
                nome = medicamento.nome,
                dosagem = medicamento.dosagem,
                excludeId = if (isEditMode) medicamento.id else null
            )
        }
    }

    /**
     * Salva o medicamento com validação e verificação de duplicatas.
     */
    fun saveMedicamento(skipValidation: Boolean = false, skipDuplicateCheck: Boolean = false) {
        val medicamento = _currentMedicamento.value

        if (medicamento == null) {
            _saveState.setError("Medicamento não inicializado")
            return
        }

        // Validar
        if (!skipValidation) {
            val validationResult = validate()
            if (validationResult.isError() && validationResult.hasCriticalErrors()) {
                val errors = validationResult.getCriticalErrors()
                    .joinToString(", ") { "${it.field}: ${it.message}" }
                _saveState.setError("Erros de validação: $errors")
                return
            }
        }

        launchWithResultState(
            liveData = _saveState,
            loadingMessage = if (isEditMode) "Atualizando medicamento..." else "Salvando medicamento..."
        ) {
            // Verificar duplicatas se solicitado
            if (!skipDuplicateCheck && !isEditMode) {
                val duplicates = repository.checkDuplicates(
                    dependentId = currentDependentId,
                    nome = medicamento.nome,
                    dosagem = medicamento.dosagem
                )

                if (duplicates.isNotEmpty()) {
                    _duplicatesState.postValue(UiState.Success(duplicates))
                    return@launchWithResultState Result.failure(
                        Exception("Medicamentos similares encontrados")
                    )
                }
            }

            // Salvar
            val result = repository.saveMedicamento(
                dependentId = currentDependentId,
                medicamento = medicamento,
                skipValidation = skipValidation
            )

            // Limpar rascunho se salvo com sucesso
            if (result.isSuccess) {
                clearDraft()
            }

            result
        }
    }

    /**
     * Salva rascunho do medicamento.
     */
    fun saveDraft(medicamento: Medicamento? = _currentMedicamento.value) {
        if (medicamento == null) return
        try {
            userPreferences.saveMedicationDraft(medicamento)
            Log.d(TAG, "Rascunho salvo com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar rascunho", e)
        }
    }

    /**
     * Limpa o rascunho salvo.
     */
    fun clearDraft() {
        try {
            userPreferences.clearMedicationDraft()
            Log.d(TAG, "Rascunho limpo")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar rascunho", e)
        }
    }

    /**
     * Verifica se há rascunho salvo.
     */
    fun hasDraft(): Boolean {
        return userPreferences.getMedicationDraft() != null
    }

    /**
     * Obtém o rascunho salvo.
     */
    fun getDraft(): Medicamento? {
        return userPreferences.getMedicationDraft()
    }

    /**
     * Atualiza o estado do wizard.
     */
    fun updateWizardStep(step: WizardStep, isComplete: Boolean = false, summary: String = "") {
        val current = _wizardState.value ?: WizardState()
        
        _wizardState.value = when (step) {
            WizardStep.STEP_1 -> current.copy(
                expandedStep = step,
                isStep1Complete = isComplete,
                summaryStep1 = summary
            )
            WizardStep.STEP_2 -> current.copy(
                expandedStep = step,
                isStep2Complete = isComplete,
                summaryStep2 = summary
            )
            WizardStep.STEP_3 -> current.copy(
                expandedStep = step,
                isStep3Complete = isComplete,
                summaryStep3 = summary
            )
            WizardStep.STEP_4 -> current.copy(
                expandedStep = step,
                summaryStep4 = summary
            )
        }
    }

    /**
     * Expande um step do wizard.
     */
    fun expandStep(step: WizardStep) {
        val current = _wizardState.value ?: WizardState()
        _wizardState.value = current.copy(expandedStep = step)
    }

    /**
     * Atualiza o wizard baseado no medicamento.
     */
    private fun updateWizardFromMedicamento(medicamento: Medicamento) {
        val state = WizardState(
            isStep1Complete = medicamento.nome.isNotBlank() && medicamento.dosagem.isNotBlank(),
            isStep2Complete = medicamento.horarios.isNotEmpty() || medicamento.isUsoEsporadico,
            isStep3Complete = true, // Estoque é opcional
            summaryStep1 = if (medicamento.nome.isNotBlank()) 
                "${medicamento.nome} - ${medicamento.dosagem}" else "Não preenchido",
            summaryStep2 = when {
                medicamento.isUsoEsporadico -> "Uso esporádico"
                medicamento.horarios.isNotEmpty() -> "${medicamento.horarios.size} horário(s)"
                else -> "Não preenchido"
            },
            summaryStep3 = if (medicamento.lotes.isNotEmpty())
                "${medicamento.lotes.size} lote(s)" else "Sem estoque",
            summaryStep4 = "Configurações adicionais"
        )
        
        _wizardState.value = state
    }

    /**
     * Reseta o estado para criar um novo medicamento.
     */
    fun reset() {
        _currentMedicamento.value = Medicamento(isUsoContinuo = true)
        _wizardState.value = WizardState()
        _saveState.setIdle()
        _duplicatesState.value = UiState.Idle
        _validationState.value = null
        isEditMode = false
    }
}

/**
 * Estado do wizard de criação/edição.
 */
data class WizardState(
    val expandedStep: WizardStep = WizardStep.STEP_1,
    val summaryStep1: String = "Não preenchido",
    val summaryStep2: String = "Não preenchido",
    val summaryStep3: String = "Não preenchido",
    val summaryStep4: String = "Não preenchido",
    val isStep1Complete: Boolean = false,
    val isStep2Complete: Boolean = false,
    val isStep3Complete: Boolean = false
)

enum class WizardStep {
    STEP_1, // Informações básicas
    STEP_2, // Horários e frequência
    STEP_3, // Estoque
    STEP_4  // Configurações adicionais
}

