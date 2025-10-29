package com.developersbeeh.medcontrol.ui.healthnotes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "HealthNotesViewModel"

@HiltViewModel
class HealthNotesViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private val _dependentId = MutableLiveData<String>()
    private val _saveStatus = MutableLiveData<Event<Boolean>>()
    val saveStatus: LiveData<Event<Boolean>> = _saveStatus

    private val _deleteStatus = MutableLiveData<Event<Boolean>>()
    val deleteStatus: LiveData<Event<Boolean>> = _deleteStatus

    val healthNotes: LiveData<List<HealthNote>> = _dependentId.switchMap { id ->
        repository.getHealthNotes(id).asLiveData()
    }

    fun initialize(dependentId: String) {
        _dependentId.value = dependentId
    }

    fun addHealthNote(type: HealthNoteType, values: Map<String, String>, note: String?) = viewModelScope.launch {
        val dependentId = _dependentId.value ?: return@launch
        val userId = repository.getCurrentUserId() ?: "dependent_user"
        val newNote = HealthNote(
            dependentId = dependentId,
            userId = userId,
            type = type,
            values = values,
            note = note,
            timestamp = LocalDateTime.now()
        )

        try {
            // Salva a anotação de saúde
            repository.saveHealthNote(dependentId, newNote)

            // ✅ LÓGICA DE SINCRONIZAÇÃO ADICIONADA
            if (type == HealthNoteType.WEIGHT) {
                values["weight"]?.let { newWeight ->
                    if (newWeight.isNotBlank()) {
                        repository.updateDependentWeight(dependentId, newWeight)
                    }
                }
            }

            // Salva o log na timeline
            activityLogRepository.saveLog(dependentId, "adicionou uma anotação de '${type.displayName}'", TipoAtividade.ANOTACAO_CRIADA)
            _saveStatus.postValue(Event(true))
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar anotação: ${e.message}", e)
            _saveStatus.postValue(Event(false))
        }
    }

    fun deleteHealthNote(note: HealthNote) = viewModelScope.launch {
        val dependentId = _dependentId.value ?: return@launch
        val result = repository.deleteHealthNote(dependentId, note.id)
        _deleteStatus.postValue(Event(result.isSuccess))
    }
}