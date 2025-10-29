// src/main/java/com/developersbeeh/medcontrol/ui/profile/EditSelfProfileHealthViewModel.kt
package com.developersbeeh.medcontrol.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Sexo
import com.developersbeeh.medcontrol.data.model.TipoSanguineo
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSelfProfileHealthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<Usuario>()
    val userProfile: LiveData<Usuario> = _userProfile

    private val _updateStatus = MutableLiveData<Event<Result<Unit>>>()
    val updateStatus: LiveData<Event<Result<Unit>>> = _updateStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _isLoading.value = true
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid
            if (userId != null) {
                userRepository.getUserProfile(userId).onSuccess { user ->
                    _userProfile.postValue(user)
                }.onFailure {
                    _updateStatus.postValue(Event(Result.failure(it)))
                }
            } else {
                _updateStatus.postValue(Event(Result.failure(Exception("Usuário não encontrado."))))
            }
            _isLoading.postValue(false)
        }
    }

    fun saveHealthData(
        dataNascimento: String, sexo: String, tipoSanguineo: String,
        peso: String, altura: String, condicoes: String, alergias: String
    ) {
        val userId = _userProfile.value?.id
        if (userId == null) {
            _updateStatus.value = Event(Result.failure(Exception("Não foi possível identificar o usuário.")))
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            // ✅ CORREÇÃO APLICADA AQUI
            // Converte o nome de exibição (ex: "A+") de volta para o nome da constante do enum (ex: "A_POSITIVO")
            val sexoEnumName = Sexo.values().firstOrNull { it.displayName == sexo }?.name ?: Sexo.NAO_INFORMADO.name
            val tipoSanguineoEnumName = TipoSanguineo.values().firstOrNull { it.displayName == tipoSanguineo }?.name ?: TipoSanguineo.NAO_SABE.name

            val result = userRepository.updateSelfProfileHealthData(
                userId, dataNascimento, sexoEnumName, tipoSanguineoEnumName, peso, altura, condicoes, alergias
            )
            _updateStatus.postValue(Event(result))
            _isLoading.postValue(false)
        }
    }
}