package com.developersbeeh.medcontrol.ui.insights

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Insight
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _dependentId = MutableLiveData<String>()

    // Observa o ID do dependente e busca os insights correspondentes em tempo real.
    val insights: LiveData<List<Insight>> = _dependentId.switchMap { id ->
        if (id.isNotBlank()) {
            firestoreRepository.getInsights(id).asLiveData()
        } else {
            MutableLiveData(emptyList())
        }
    }

    fun initialize(dependentId: String) {
        if (_dependentId.value == dependentId) return
        _dependentId.value = dependentId
    }

    // Função para ser chamada quando o usuário visualiza a tela.
    fun markAllAsRead() {
        val id = _dependentId.value
        if (id != null) {
            viewModelScope.launch {
                firestoreRepository.markInsightsAsRead(id)
            }
        }
    }
}