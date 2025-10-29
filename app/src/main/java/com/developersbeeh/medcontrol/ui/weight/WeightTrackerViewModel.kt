package com.developersbeeh.medcontrol.ui.weight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightScreenState(
    val dependent: Dependente,
    val history: List<HealthNote>
)

@HiltViewModel
class WeightTrackerViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val application: Application // Injetado
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<WeightScreenState>>()
    val uiState: LiveData<UiState<WeightScreenState>> = _uiState

    fun initialize(dependentId: String) {
        if (dependentId.isBlank()) {
            _uiState.value = UiState.Error(application.getString(R.string.error_invalid_dependent_id))
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val dependent = firestoreRepository.getDependente(dependentId)
            if (dependent == null) {
                _uiState.postValue(UiState.Error(application.getString(R.string.error_dependent_not_found)))
                return@launch
            }

            firestoreRepository.getHealthNotes(dependentId)
                .collectLatest { allNotes ->
                    val weightNotes = allNotes
                        .filter { it.type == HealthNoteType.WEIGHT && it.values["weight"]?.isNotBlank() == true }
                        .sortedBy { it.timestamp }
                    _uiState.postValue(UiState.Success(WeightScreenState(dependent, weightNotes)))
                }
        }
    }
}