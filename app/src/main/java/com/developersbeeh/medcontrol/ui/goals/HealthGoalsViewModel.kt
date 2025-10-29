// src/main/java/com/developersbeeh/medcontrol/ui/goals/HealthGoalsViewModel.kt
package com.developersbeeh.medcontrol.ui.goals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthGoalsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _dependent = MutableLiveData<Dependente?>()
    val dependent: LiveData<Dependente?> = _dependent

    private val _saveStatus = MutableLiveData<Event<Result<Unit>>>()
    val saveStatus: LiveData<Event<Result<Unit>>> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDependent(dependentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepository.getDependente(dependentId)
            _dependent.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun saveGoals(
        weightGoal: String,
        hydrationGoal: Int,
        calorieGoal: Int,
        activityGoal: Int
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentDependent = _dependent.value
            if (currentDependent == null) {
                _saveStatus.postValue(Event(Result.failure(Exception("Dependente não encontrado."))))
                _isLoading.postValue(false)
                return@launch
            }

            val updatedDependent = currentDependent.copy(
                pesoMeta = weightGoal,
                metaHidratacaoMl = hydrationGoal,
                metaCaloriasDiarias = calorieGoal,
                metaAtividadeMinutos = activityGoal
            )

            val result = firestoreRepository.updateDependent(updatedDependent)
            _saveStatus.postValue(Event(result))
            _isLoading.postValue(false)
        }
    }
}