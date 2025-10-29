package com.developersbeeh.medcontrol.ui.hydration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.Hidratacao
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HydrationScreenState(
    val dependent: Dependente,
    val history: List<Hidratacao>
)

@HiltViewModel
class HydrationTrackerViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val application: Application // Injetado
) : AndroidViewModel(application) { // Herda de AndroidViewModel

    private val _uiState = MutableLiveData<UiState<HydrationScreenState>>()
    val uiState: LiveData<UiState<HydrationScreenState>> = _uiState

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

            val thirtyDaysAgo = LocalDate.now().minusDays(29)
            firestoreRepository.getHidratacaoHistory(dependentId, thirtyDaysAgo, LocalDate.now())
                .collectLatest { records ->
                    _uiState.postValue(UiState.Success(HydrationScreenState(dependent, records)))
                }
        }
    }
}