// src/main/java/com/developersbeeh/medcontrol/ui/documents/HealthDocumentsViewModel.kt
package com.developersbeeh.medcontrol.ui.documents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import com.developersbeeh.medcontrol.data.model.TipoDocumento
import com.developersbeeh.medcontrol.data.repository.DocumentRepository
import com.developersbeeh.medcontrol.util.Event
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthDocumentsViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private lateinit var dependentId: String
    private lateinit var dependentName: String

    // ✅ Usa StateFlow para o filtro, que é mais moderno e integra melhor com corrotinas
    private val filterType = MutableStateFlow<TipoDocumento?>(null)

    private val _documents = MutableLiveData<UiState<List<DocumentoSaude>>>()
    val documents: LiveData<UiState<List<DocumentoSaude>>> = _documents

    private val _navigationEvent = MutableLiveData<Event<NavDirections>>()
    val navigationEvent: LiveData<Event<NavDirections>> = _navigationEvent

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    fun initialize(dependentId: String, dependentName: String) {
        if (this::dependentId.isInitialized && this.dependentId == dependentId) return
        this.dependentId = dependentId
        this.dependentName = dependentName

        loadDocuments()
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            _documents.value = UiState.Loading
            // Combina o fluxo de documentos do repositório com o fluxo do filtro
            documentRepository.getDocuments(dependentId).combine(filterType) { allDocs, filter ->
                if (filter == null) {
                    allDocs
                } else {
                    allDocs.filter { it.tipo == filter }
                }
            }.collect { filteredDocs ->
                _documents.postValue(UiState.Success(filteredDocs))
            }
        }
    }

    fun applyFilter(type: TipoDocumento?) {
        filterType.value = type
    }

    fun onAddDocumentClicked() {
        val action = HealthDocumentsFragmentDirections
            .actionHealthDocumentsFragmentToAddEditDocumentFragment(
                dependentId = dependentId,
                dependentName = dependentName,
                documento = null
            )
        _navigationEvent.value = Event(action)
    }

    fun onDocumentSelected(documento: DocumentoSaude) {
        val action = HealthDocumentsFragmentDirections
            .actionHealthDocumentsFragmentToAddEditDocumentFragment(
                dependentId = dependentId,
                dependentName = dependentName,
                documento = documento
            )
        _navigationEvent.value = Event(action)
    }

    fun deleteDocument(documento: DocumentoSaude) {
        viewModelScope.launch {
            documentRepository.deleteDocument(documento).onSuccess {
                _actionFeedback.postValue(Event("Documento excluído com sucesso."))
            }.onFailure {
                _actionFeedback.postValue(Event("Falha ao excluir o documento."))
            }
        }
    }
}