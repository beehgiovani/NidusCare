// src/main/java/com/developersbeeh/medcontrol/ui/pharmacy/PharmacyMedicationSelectionViewModel.kt
package com.developersbeeh.medcontrol.ui.pharmacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.util.Event
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

data class SelectableMedication(
    val medicationName: String,
    val unit: String,
    val totalStock: Double,
    val dependentNames: List<String>,
    val isLowStock: Boolean,
    var isSelected: Boolean = false
)

sealed class WhatsAppNavigation {
    data class SendMessage(val phoneNumber: String, val message: String) : WhatsAppNavigation()
}

@HiltViewModel
class PharmacyMedicationSelectionViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<SelectableMedication>>>()
    val uiState: LiveData<UiState<List<SelectableMedication>>> = _uiState

    private val _whatsAppEvent = MutableLiveData<Event<WhatsAppNavigation>>()
    val whatsAppEvent: LiveData<Event<WhatsAppNavigation>> = _whatsAppEvent

    private var allMedications = mutableListOf<SelectableMedication>()
    private var isLowStockFilterActive = true // Controla o estado do filtro atual

    init {
        loadAllMedications()
    }

    private fun loadAllMedications() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val dependents = firestoreRepository.getDependentes().first()
                val allMedsFromAllDependents = mutableListOf<Pair<com.developersbeeh.medcontrol.data.model.Medicamento, String>>()

                dependents.forEach { dependent ->
                    medicationRepository.getMedicamentos(dependent.id).first().forEach { med ->
                        allMedsFromAllDependents.add(Pair(med, dependent.nome))
                    }
                }

                val consolidatedMeds = allMedsFromAllDependents
                    .groupBy { it.first.nome.lowercase().trim() }
                    .map { (_, medPairs) ->
                        val firstMed = medPairs.first().first
                        val isLowStock = firstMed.nivelDeAlertaEstoque > 0 && firstMed.estoqueAtualTotal <= firstMed.nivelDeAlertaEstoque
                        SelectableMedication(
                            medicationName = firstMed.nome,
                            unit = firstMed.unidadeDeEstoque,
                            totalStock = medPairs.sumOf { it.first.estoqueAtualTotal },
                            dependentNames = medPairs.map { it.second }.distinct(),
                            isLowStock = isLowStock,
                            isSelected = isLowStock // ✅ MELHORIA: Pré-seleciona os itens de estoque baixo
                        )
                    }
                    .sortedBy { it.medicationName }

                allMedications.clear()
                allMedications.addAll(consolidatedMeds)
                filterAndPostList(showLowStockOnly = true)

            } catch (e: Exception) {
                _uiState.postValue(UiState.Error("Erro ao carregar medicamentos."))
            }
        }
    }

    fun filterList(showLowStockOnly: Boolean) {
        isLowStockFilterActive = showLowStockOnly
        filterAndPostList(showLowStockOnly)
    }

    private fun filterAndPostList(showLowStockOnly: Boolean) {
        val listToShow = if (showLowStockOnly) {
            allMedications.filter { it.isLowStock }
        } else {
            allMedications
        }
        // .toList() cria uma nova instância da lista, garantindo que o DiffUtil do adapter a detecte.
        _uiState.postValue(UiState.Success(listToShow.toList()))
    }

    fun toggleMedicationSelection(medication: SelectableMedication) {
        // ✅ CORREÇÃO: Lógica de seleção imutável
        // Em vez de modificar a lista, criamos uma nova com o item atualizado.
        allMedications = allMedications.map {
            if (it.medicationName == medication.medicationName) {
                it.copy(isSelected = !it.isSelected) // Cria uma cópia do objeto com o 'isSelected' trocado
            } else {
                it
            }
        }.toMutableList()

        // Re-aplica o filtro atual para atualizar a UI corretamente
        filterAndPostList(isLowStockFilterActive)
    }

    fun prepareWhatsAppMessage(pharmacyName: String, pharmacyPhoneNumber: String) {
        val selectedMeds = allMedications.filter { it.isSelected }
        if (selectedMeds.isEmpty()) {
            return
        }

        val messageBuilder = StringBuilder()
        messageBuilder.append("Olá, ${pharmacyName}! Gostaria de verificar a disponibilidade e o valor dos seguintes medicamentos:\n\n")
        selectedMeds.forEach {
            messageBuilder.append("- ${it.medicationName}\n")
        }
        messageBuilder.append("\nObrigado(a)!")

        val sanitizedNumber = pharmacyPhoneNumber.filter { it.isDigit() }
        val finalNumber = if (sanitizedNumber.length > 8) "55$sanitizedNumber" else ""

        _whatsAppEvent.postValue(Event(WhatsAppNavigation.SendMessage(finalNumber, messageBuilder.toString())))
    }
}