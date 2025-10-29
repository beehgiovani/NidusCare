package com.developersbeeh.medcontrol.ui.dependents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DependentDiaryViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val activityLogRepository: ActivityLogRepository // ✅ 1. INJEÇÃO DO NOVO REPOSITÓRIO
) : ViewModel() {

    private val _saveStatus = MutableLiveData<Event<Boolean>>()
    val saveStatus: LiveData<Event<Boolean>> = _saveStatus

    private lateinit var dependentId: String

    fun initialize(id: String) {
        dependentId = id
    }

    // ✅ 2. FUNÇÃO LOCAL REMOVIDA
    /*
    private suspend fun saveLog(descricao: String) {
        ...
    }
    */


    fun saveDiaryEntry(mood: String?, symptom: String?, notes: String?) {
        if (mood.isNullOrBlank() && symptom.isNullOrBlank()) {
            _saveStatus.postValue(Event(false)) // Nada para salvar
            return
        }

        viewModelScope.launch {
            var success = true
            // Salva o humor como uma anotação separada
            if (!mood.isNullOrBlank()) {
                val moodNote = HealthNote(
                    dependentId = dependentId,
                    userId = "dependent_user",
                    type = HealthNoteType.MOOD,
                    values = mapOf("mood" to mood),
                    note = notes
                )
                val result = firestoreRepository.saveHealthNote(dependentId, moodNote)
                if(result.isSuccess){
                    // ✅ 3. CHAMADA SUBSTITUÍDA
                    activityLogRepository.saveLog(dependentId, "registrou o humor: '$mood'", TipoAtividade.ANOTACAO_CRIADA)
                } else {
                    success = false
                }
            }

            // Salva o sintoma como uma anotação separada
            if (!symptom.isNullOrBlank()) {
                val symptomNote = HealthNote(
                    dependentId = dependentId,
                    userId = "dependent_user",
                    type = HealthNoteType.SYMPTOM,
                    values = mapOf("symptom" to symptom),
                    // Adiciona as notas apenas uma vez, no primeiro item que for salvo
                    note = if (mood.isNullOrBlank()) notes else null
                )
                val result = firestoreRepository.saveHealthNote(dependentId, symptomNote)
                if(result.isSuccess){
                    // ✅ 3. CHAMADA SUBSTITUÍDA
                    activityLogRepository.saveLog(dependentId, "registrou um sintoma: '$symptom'", TipoAtividade.ANOTACAO_CRIADA)
                } else {
                    success = false
                }
            }
            _saveStatus.postValue(Event(success))
        }
    }
}