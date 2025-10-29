package com.developersbeeh.medcontrol.ui.meals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.Refeicao
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MealScreenState(
    val dependent: Dependente,
    val history: List<Refeicao>
)

@HiltViewModel
class MealTrackerViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val application: Application // Injetado
) : AndroidViewModel(application) { // Herda de AndroidViewModel

    private val _uiState = MutableLiveData<UiState<MealScreenState>>()
    val uiState: LiveData<UiState<MealScreenState>> = _uiState

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
            firestoreRepository.getRefeicoesHistoryForTimeline(dependentId, thirtyDaysAgo)
                .collectLatest { records ->
                    _uiState.postValue(UiState.Success(MealScreenState(dependent, records)))
                }
        }
    }
}