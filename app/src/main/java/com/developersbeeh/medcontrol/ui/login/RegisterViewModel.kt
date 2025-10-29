// src/main/java/com/developersbeeh/medcontrol/ui/login/RegisterViewModel.kt
package com.developersbeeh.medcontrol.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Conquista
import com.developersbeeh.medcontrol.data.model.TipoConquista
import com.developersbeeh.medcontrol.data.model.TipoConquista.*
import com.developersbeeh.medcontrol.data.repository.AchievementRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _authStatus = MutableLiveData<AuthStatus>()
    val authStatus: LiveData<AuthStatus> = _authStatus

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    private val _navigateToHomeEvent = MutableLiveData<Event<FirebaseUser>>()
    val navigateToHomeEvent: LiveData<Event<FirebaseUser>> = _navigateToHomeEvent

    fun createAccount(
        name: String, email: String, pass: String, confirmPass: String,
        dataNascimento: String, sexo: String, tipoSanguineo: String,
        peso: String, altura: String, condicoes: String, alergias: String
    ) {
        if (name.isBlank() || email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _errorEvent.value = Event("Os campos com * são obrigatórios.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorEvent.value = Event("Por favor, insira um formato de e-mail válido.")
            return
        }
        if (pass != confirmPass) {
            _errorEvent.value = Event("As senhas não coincidem.")
            return
        }
        if (pass.length < 6) {
            _errorEvent.value = Event("A senha deve ter pelo menos 6 caracteres.")
            return
        }

        _authStatus.value = AuthStatus.LOADING

        viewModelScope.launch {
            val result = userRepository.createUser(
                name, email, pass,
                dataNascimento, sexo, tipoSanguineo, peso, altura, condicoes, alergias
            )
            // ✅ LÓGICA DE SUCESSO ATUALIZADA
            result.onSuccess { (user, selfCareProfileId) -> // Desestrutura o Pair retornado
                // Verifica se os campos de saúde opcionais foram preenchidos
                val healthFields = listOf(dataNascimento, sexo, tipoSanguineo, peso, altura, condicoes, alergias)
                if (healthFields.all { it.isNotBlank() }) {
                    // Concede a conquista usando o ID do perfil retornado diretamente
                    val conquista = Conquista(
                        id = PERFIL_COMPLETO.name,
                        tipo = PERFIL_COMPLETO
                    )
                    achievementRepository.awardAchievement(selfCareProfileId, conquista)
                }

                _authStatus.value = AuthStatus.AUTHENTICATED
                _navigateToHomeEvent.value = Event(user)
            }.onFailure { e ->
                _authStatus.value = AuthStatus.ERROR
                _errorEvent.value = Event(e.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }
}