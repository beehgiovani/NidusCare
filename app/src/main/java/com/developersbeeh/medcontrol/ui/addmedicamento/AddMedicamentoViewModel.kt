package com.developersbeeh.medcontrol.ui.addmedicamento

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Conquista
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoConquista
import com.developersbeeh.medcontrol.data.repository.AchievementRepository
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AddMedicamentoVM"


data class AddMedicamentoUiState(
    val expandedStep: WizardStep = WizardStep.STEP_1,
    val summaryStep1: String = "Não preenchido",
    val summaryStep2: String = "Não preenchido",
    val summaryStep3: String = "Não preenchido",
    val summaryStep4: String = "Não preenchido",
    val isStep1Complete: Boolean = false,
    val isStep2Complete: Boolean = false,
    val isStep3Complete: Boolean = false
)

@HiltViewModel
class AddMedicamentoViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val achievementRepository: AchievementRepository,
    private val medicationRepository: MedicationRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableLiveData(AddMedicamentoUiState())
    val uiState: LiveData<AddMedicamentoUiState> = _uiState

    private val _saveSuccessEvent = MutableLiveData<Event<Unit>>()
    val saveSuccessEvent: LiveData<Event<Unit>> = _saveSuccessEvent

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    private val _showDuplicateMedicationDialog = MutableLiveData<Event<Medicamento>>()
    val showDuplicateMedicationDialog: LiveData<Event<Medicamento>> = _showDuplicateMedicationDialog

    private var allOtherMedications: List<Pair<Medicamento, String>> = emptyList()

    private val _existingMedicationFound = MutableLiveData<Event<Pair<Medicamento, String>?>>()
    val existingMedicationFound: LiveData<Event<Pair<Medicamento, String>?>> = _existingMedicationFound

    // ✅ ADIÇÃO: Armazena o estado do medicamento que está sendo editado/criado
    private val _medicamentoParaEditar = MutableLiveData<Medicamento>()
    val medicamentoParaEditar: LiveData<Medicamento> = _medicamentoParaEditar

    fun initialize(medicamento: Medicamento?) {
        // ✅ CORREÇÃO: Define um estado padrão se for um novo medicamento
        if (_medicamentoParaEditar.value != null) return // Já inicializado

        if (medicamento == null) {
            // Se for um NOVO medicamento, define o padrão como Uso Contínuo
            _medicamentoParaEditar.value = Medicamento(isUsoContinuo = true)
        } else {
            // Se for EDITANDO, carrega o medicamento existente
            _medicamentoParaEditar.value = medicamento
        }
    }

    fun hasDraft(): Boolean {
        return userPreferences.getMedicationDraft() != null
    }

    fun getDraft(): Medicamento? {
        return userPreferences.getMedicationDraft()
    }

    fun saveDraft(medicamento: Medicamento) {
        userPreferences.saveMedicationDraft(medicamento)
    }

    fun clearDraft() {
        userPreferences.clearMedicationDraft()
    }

    fun loadAllCaregiverMedications(currentDependentId: String) {
        viewModelScope.launch {
            try {
                val allDependents = firestoreRepository.getDependentes().first()
                val otherDependents = allDependents.filter { it.id != currentDependentId }

                val medsList = mutableListOf<Pair<Medicamento, String>>()
                otherDependents.forEach { dependent ->
                    val meds = medicationRepository.getMedicamentos(dependent.id).first()
                    meds.forEach { med ->
                        medsList.add(med to dependent.nome)
                    }
                }
                allOtherMedications = medsList
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar medicamentos de outros dependentes", e)
                allOtherMedications = emptyList()
            }
        }
    }

    fun searchExistingMedication(query: String) {
        if (query.length < 3) {
            _existingMedicationFound.postValue(Event(null))
            return
        }
        val foundPair = allOtherMedications.find { (medicamento, _) ->
            medicamento.nome.equals(query, ignoreCase = true)
        }
        _existingMedicationFound.postValue(Event(foundPair))
    }

    fun onStepHeaderClicked(step: WizardStep) {
        val currentStep = _uiState.value?.expandedStep
        _uiState.value = _uiState.value?.copy(
            expandedStep = if (currentStep == step) WizardStep.STEP_1 else step
        )
    }

    fun updateSummaries(
        nome: String, tipoAdmin: String, dosagem: String, frequencia: String,
        duracao: String, notificacoes: Boolean, estoque: String, opcionais: String
    ) {
        val currentState = _uiState.value ?: AddMedicamentoUiState()
        _uiState.value = currentState.copy(
            summaryStep1 = if (nome.isNotBlank()) "$nome, $tipoAdmin" else "Não preenchido",
            summaryStep2 = if (dosagem.isNotBlank() && frequencia.isNotBlank()) "$dosagem, $frequencia" else "Não preenchido",
            summaryStep3 = if (duracao.isNotBlank()) "$duracao, ${if(notificacoes) "com" else "sem"} notificações" else "Não preenchido",
            summaryStep4 = if (estoque.isNotBlank() || opcionais.isNotBlank()) "Detalhes preenchidos" else "Não preenchido",
            isStep1Complete = nome.isNotBlank(),
            isStep2Complete = dosagem.isNotBlank() && frequencia.isNotBlank(),
            isStep3Complete = duracao.isNotBlank()
        )
    }

    fun saveMedicamento(medicamento: Medicamento, dependentId: String, isEditing: Boolean) {
        viewModelScope.launch {
            if (!isEditing) {
                val allMeds = medicationRepository.getMedicamentos(dependentId).first()
                val existingMed = allMeds.find { it.nome.equals(medicamento.nome, ignoreCase = true) }

                if (existingMed != null) {
                    _showDuplicateMedicationDialog.postValue(Event(existingMed))
                    return@launch
                }

                val isFirstMedication = allMeds.isEmpty()
                performSave(medicamento, dependentId, isFirstMedication)

            } else {
                performSave(medicamento, dependentId, isFirstMedication = false)
            }
        }
    }

    private fun performSave(medicamento: Medicamento, dependentId: String, isFirstMedication: Boolean) = viewModelScope.launch {
        try {
            medicationRepository.saveMedicamento(dependentId, medicamento)
            clearDraft()

            if (isFirstMedication) {
                val conquista = Conquista(
                    id = TipoConquista.PRIMEIRO_MEDICAMENTO.name,
                    tipo = TipoConquista.PRIMEIRO_MEDICAMENTO
                )
                achievementRepository.awardAchievement(dependentId, conquista)
                Log.i(TAG, "Conquista PRIMEIRO_MEDICAMENTO concedida para $dependentId")
            }

            _saveSuccessEvent.postValue(Event(Unit))
        } catch (e: Exception) {
            _errorEvent.postValue(Event("Erro ao salvar o medicamento: ${e.message}"))
            Log.e(TAG, "Erro ao salvar o medicamento: ${e.message}", e)
        }
    }
}