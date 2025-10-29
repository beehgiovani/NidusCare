// src/main/java/com/developersbeeh/medcontrol/ui/documents/AddEditDocumentViewModel.kt
package com.developersbeeh.medcontrol.ui.documents

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.data.repository.AchievementRepository
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
import com.developersbeeh.medcontrol.data.repository.DocumentRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddEditDocumentViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val achievementRepository: AchievementRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _saveStatus = MutableLiveData<Event<Result<Unit>>>()
    val saveStatus: LiveData<Event<Result<Unit>>> = _saveStatus

    // ✅ NOVOS EVENTOS PARA CONTROLAR O DIÁLOGO DE CARREGAMENTO
    private val _showLoading = MutableLiveData<Event<String>>()
    val showLoading: LiveData<Event<String>> = _showLoading

    private val _hideLoading = MutableLiveData<Event<Unit>>()
    val hideLoading: LiveData<Event<Unit>> = _hideLoading

    // ❌ O LiveData isLoading antigo não é mais necessário para a UI
    // private val _isLoading = MutableLiveData<Boolean>(false)
    // val isLoading: LiveData<Boolean> = _isLoading

    private fun getFileName(uri: Uri): String {
        var fileName = "documento"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }
        return fileName
    }

    fun saveDocument(
        dependentId: String,
        documentoToEdit: DocumentoSaude?,
        titulo: String,
        tipo: TipoDocumento,
        data: LocalDate,
        medico: String?,
        laboratorio: String?,
        anotacoes: String?,
        fileUri: Uri?
    ) {
        if (titulo.isBlank() || (documentoToEdit == null && fileUri == null)) {
            _saveStatus.postValue(Event(Result.failure(IllegalArgumentException("Título e arquivo são obrigatórios."))))
            return
        }

        _showLoading.postValue(Event("Salvando documento..."))

        viewModelScope.launch {
            try {
                val isFirstDocument = documentoToEdit == null && documentRepository.getDocuments(dependentId).first().isEmpty()

                val isPremium = userPreferences.isPremium()
                if (!isPremium && !isFirstDocument && documentoToEdit == null) {
                    val existingDocuments = documentRepository.getDocuments(dependentId).first()
                    if (existingDocuments.size >= 5) {
                        _saveStatus.postValue(Event(Result.failure(Exception("O plano gratuito permite até 5 uploads de documentos. Faça upgrade para ter acesso ilimitado."))))
                        return@launch
                    }
                }

                val documento = documentoToEdit?.copy(
                    titulo = titulo, tipo = tipo, dataDocumento = data.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    medicoSolicitante = medico, laboratorio = laboratorio, anotacoes = anotacoes,
                    fileName = if (fileUri != null) getFileName(fileUri) else documentoToEdit.fileName
                ) ?: DocumentoSaude(
                    dependentId = dependentId, titulo = titulo, tipo = tipo,
                    dataDocumento = data.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    medicoSolicitante = medico, laboratorio = laboratorio, anotacoes = anotacoes,
                    fileName = if (fileUri != null) getFileName(fileUri) else ""
                )

                val result = documentRepository.saveDocument(documento, fileUri)
                if (result.isSuccess) {
                    if (isFirstDocument) {
                        val conquista = Conquista(id = TipoConquista.PRIMEIRO_DOCUMENTO.name, tipo = TipoConquista.PRIMEIRO_DOCUMENTO)
                        achievementRepository.awardAchievement(dependentId, conquista)
                    }
                    if (documentoToEdit == null) {
                        activityLogRepository.saveLog(dependentId, "adicionou o documento '${documento.titulo}'", TipoAtividade.DOCUMENTO_ADICIONADO)
                    }
                }

                _saveStatus.postValue(Event(result))
            } finally {
                _hideLoading.postValue(Event(Unit))
            }
        }
    }
}