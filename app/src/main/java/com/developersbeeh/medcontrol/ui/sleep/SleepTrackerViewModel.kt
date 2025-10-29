package com.developersbeeh.medcontrol.ui.sleep

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.RegistroSono
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SleepTrackerViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val application: Application // Injetado
) : AndroidViewModel(application) { // Herda de AndroidViewModel

    private val _uiState = MutableLiveData<UiState<List<RegistroSono>>>()
    val uiState: LiveData<UiState<List<RegistroSono>>> = _uiState

    fun initialize(dependentId: String) {
        if (dependentId.isBlank()) {
            _uiState.value = UiState.Error(application.getString(R.string.error_invalid_dependent_id))
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val thirtyDaysAgo = LocalDate.now().minusDays(29)
            firestoreRepository.getSonoHistory(dependentId, thirtyDaysAgo, LocalDate.now())
                .collectLatest { records ->
                    _uiState.postValue(UiState.Success(records))
                }
        }
    }
}