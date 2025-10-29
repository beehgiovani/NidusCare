package com.developersbeeh.medcontrol.ui.analysis // Pacote corrigido

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.developersbeeh.medcontrol.data.model.AnalysisHistory
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnalysisHistoryViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _dependentId = MutableLiveData<String>()

    val analysisHistory: LiveData<List<AnalysisHistory>> = _dependentId.switchMap { id ->
        repository.getAnalysisHistory(id).asLiveData()
    }

    fun initialize(dependentId: String) {
        if (_dependentId.value == dependentId) return
        _dependentId.value = dependentId
    }
}