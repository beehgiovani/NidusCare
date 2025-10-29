package com.developersbeeh.medcontrol.ui.dependents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkDependentViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    application: Application
) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _loginResult = MutableLiveData<Event<Dependente>>()
    val loginResult: LiveData<Event<Dependente>> = _loginResult

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun onLoginClicked(code: String, pass: String) {
        if (code.isBlank() || pass.isBlank()) {
            _errorEvent.value = Event("Código e senha são obrigatórios.")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.loginDependente(code, pass)
            result.onSuccess { dependente ->
                onLoginSuccess(dependente)
            }.onFailure {
                _errorEvent.postValue(Event(it.message ?: "Erro desconhecido."))
            }
            _isLoading.postValue(false)
        }
    }

    private fun onLoginSuccess(dependente: Dependente) {
        userPreferences.saveIsCaregiver(false)
        userPreferences.saveDependentId(dependente.id)
        userPreferences.saveUserName(dependente.nome) // Salva o nome do dependente como nome de usuário
        userPreferences.saveDependentPermissions(dependente.permissoes)
        _loginResult.postValue(Event(dependente))
    }
}