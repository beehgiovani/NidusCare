// src/main/java/com/developersbeeh/medcontrol/ui/caregiver/AddEditDependentViewModel.kt
package com.developersbeeh.medcontrol.ui.caregiver

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.Sexo
import com.developersbeeh.medcontrol.data.model.TipoSanguineo
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WizardStep { STEP_1, STEP_2, STEP_3, STEP_4 }

@HiltViewModel
class AddEditDependentViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _dependente = MutableLiveData<Dependente?>()
    val dependente: LiveData<Dependente?> = _dependente

    private val _saveResult = MutableLiveData<Event<Result<Unit>>>()
    val saveResult: LiveData<Event<Result<Unit>>> = _saveResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _currentStep = MutableLiveData(WizardStep.STEP_1)
    val currentStep: LiveData<WizardStep> = _currentStep

    fun loadDependent(dependente: Dependente) {
        _dependente.value = dependente
    }

    fun nextStep() {
        val next = when (_currentStep.value) {
            WizardStep.STEP_1 -> WizardStep.STEP_2
            WizardStep.STEP_2 -> WizardStep.STEP_3
            WizardStep.STEP_3 -> WizardStep.STEP_4 // ✅ NAVEGA PARA O PASSO 4
            else -> null
        }
        if (next != null) {
            _currentStep.value = next
        }
    }

    fun previousStep() {
        val prev = when (_currentStep.value) {
            WizardStep.STEP_2 -> WizardStep.STEP_1
            WizardStep.STEP_3 -> WizardStep.STEP_2
            WizardStep.STEP_4 -> WizardStep.STEP_3 // ✅ NAVEGA PARA O PASSO 3
            else -> null
        }
        if (prev != null) {
            _currentStep.value = prev
        }
    }

    fun saveDependent(
        id: String?,
        nome: String,
        dataDeNascimento: String,
        sexo: String,
        peso: String,
        altura: String,
        tipoSanguineo: String,
        condicoes: String,
        alergias: String,
        observacoes: String,
        contatoNome: String,
        contatoTelefone: String,
        metaHidratacao: Int,
        metaAtividade: Int,
        metaCalorias: Int,
        metaPeso: String,
        imageUri: Uri?,
        permissoes: Map<String, Boolean>
    ) {
        if (nome.isBlank()) {
            _saveResult.value = Event(Result.failure(IllegalArgumentException("O nome é obrigatório.")))
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val sexoEnum = Sexo.values().firstOrNull { it.displayName == sexo } ?: Sexo.NAO_INFORMADO
            val tipoSanguineoEnum = TipoSanguineo.values().firstOrNull { it.displayName == tipoSanguineo } ?: TipoSanguineo.NAO_SABE

            val dependenteToSave = _dependente.value?.copy(
                nome = nome,
                dataDeNascimento = dataDeNascimento,
                sexo = sexoEnum.name,
                peso = peso,
                altura = altura,
                tipoSanguineo = tipoSanguineoEnum.name,
                condicoesPreexistentes = condicoes,
                alergias = alergias,
                observacoesMedicas = observacoes,
                contatoEmergenciaNome = contatoNome,
                contatoEmergenciaTelefone = contatoTelefone,
                metaHidratacaoMl = metaHidratacao,
                metaAtividadeMinutos = metaAtividade,
                metaCaloriasDiarias = metaCalorias,
                pesoMeta = metaPeso,
                permissoes = permissoes
            ) ?: Dependente(
                id = id ?: "",
                nome = nome,
                dataDeNascimento = dataDeNascimento,
                sexo = sexoEnum.name,
                peso = peso,
                altura = altura,
                tipoSanguineo = tipoSanguineoEnum.name,
                condicoesPreexistentes = condicoes,
                alergias = alergias,
                observacoesMedicas = observacoes,
                contatoEmergenciaNome = contatoNome,
                contatoEmergenciaTelefone = contatoTelefone,
                metaHidratacaoMl = metaHidratacao,
                metaAtividadeMinutos = metaAtividade,
                metaCaloriasDiarias = metaCalorias,
                pesoMeta = metaPeso,
                permissoes = permissoes
            )

            val isNewDependent = dependenteToSave.id.isBlank()
            val isPremium = userRepository.isUserPremium(userRepository.getCurrentUser()?.uid ?: "")

            if (isNewDependent && !isPremium) {
                val dependentCount = firestoreRepository.getDependentCountForCurrentUser()
                if (dependentCount >= 2) {
                    _saveResult.postValue(Event(Result.failure(Exception("O plano gratuito permite 1 perfil de autocuidado e 1 dependente adicional. Faça upgrade para adicionar mais."))))
                    _isLoading.postValue(false)
                    return@launch
                }
            }

            val initialSaveResult = if (isNewDependent) {
                firestoreRepository.createDependent(dependenteToSave)
            } else {
                firestoreRepository.updateDependent(dependenteToSave).map { dependenteToSave }
            }

            initialSaveResult.onSuccess { savedDependente ->
                if (imageUri != null) {
                    userRepository.uploadDependentPhoto(savedDependente.id, imageUri).onSuccess { photoUrl ->
                        val finalDependente = savedDependente.copy(photoUrl = photoUrl)
                        val finalResult = firestoreRepository.updateDependent(finalDependente)
                        _saveResult.postValue(Event(finalResult.map { }))
                    }.onFailure { _saveResult.postValue(Event(Result.failure(it))) }
                } else {
                    _saveResult.postValue(Event(Result.success(Unit)))
                }
            }.onFailure {
                _saveResult.postValue(Event(Result.failure(it)))
            }
            _isLoading.postValue(false)
        }
    }
}