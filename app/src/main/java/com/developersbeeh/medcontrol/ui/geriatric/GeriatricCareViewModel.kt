// src/main/java/com/developersbeeh/medcontrol/ui/geriatric/GeriatricCareViewModel.kt
package com.developersbeeh.medcontrol.ui.geriatric

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.ArtigoEducativo
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.model.Reminder
import com.developersbeeh.medcontrol.data.repository.EducationRepository
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.ReminderRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

enum class GeriatricReminderType {
    PRESSAO, VITAMINA_D, ATIVIDADE_LEVE
}

data class VitalsState(
    val lastBloodPressure: HealthNote? = null,
    val lastBloodSugar: HealthNote? = null
)

@HiltViewModel
class GeriatricCareViewModel @Inject constructor(
    private val educationRepository: EducationRepository,
    private val reminderRepository: ReminderRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _articles = MutableLiveData<List<ArtigoEducativo>>()
    val articles: LiveData<List<ArtigoEducativo>> = _articles

    private val _vitalsState = MutableLiveData<VitalsState>()
    val vitalsState: LiveData<VitalsState> = _vitalsState

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    init {
        loadArticles()
    }

    fun initialize(dependentId: String) {
        loadRecentVitals(dependentId)
    }

    private fun loadArticles() {
        _articles.value = educationRepository.getArtigos().filter { it.categoria == "Idosos" }
    }

    private fun loadRecentVitals(dependentId: String) {
        viewModelScope.launch {
            val healthNotes = firestoreRepository.getHealthNotes(dependentId).first()
            val lastPressure = healthNotes
                .filter { it.type == HealthNoteType.BLOOD_PRESSURE }
                .maxByOrNull { it.timestamp }
            val lastSugar = healthNotes
                .filter { it.type == HealthNoteType.BLOOD_SUGAR }
                .maxByOrNull { it.timestamp }

            _vitalsState.postValue(VitalsState(lastPressure, lastSugar))
        }
    }

    fun addPredefinedReminder(dependentId: String, type: GeriatricReminderType) {
        viewModelScope.launch {
            val userId = firestoreRepository.getCurrentUserId() ?: "caregiver_user"

            // ✅ CORREÇÃO: Cria o objeto primeiro e DEPOIS define o horário.
            val newReminder = when (type) {
                GeriatricReminderType.PRESSAO -> Reminder(
                    userId = userId,
                    type = "Medir Pressão Arterial"
                ).apply { time = LocalTime.of(8, 0) }

                GeriatricReminderType.VITAMINA_D -> Reminder(
                    userId = userId,
                    type = "Tomar Vitamina D"
                ).apply { time = LocalTime.of(12, 30) }

                GeriatricReminderType.ATIVIDADE_LEVE -> Reminder(
                    userId = userId,
                    type = "Fazer caminhada leve"
                ).apply { time = LocalTime.of(16, 0) }
            }

            val result = reminderRepository.saveReminder(dependentId, newReminder)
            if (result.isSuccess) {
                _actionFeedback.postValue(Event("Lembrete '${newReminder.type}' adicionado!"))
            } else {
                _actionFeedback.postValue(Event("Erro ao adicionar lembrete."))
            }
        }
    }
}