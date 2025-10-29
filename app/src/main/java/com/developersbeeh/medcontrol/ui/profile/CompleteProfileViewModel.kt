package com.developersbeeh.medcontrol.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Conquista
import com.developersbeeh.medcontrol.data.model.TipoConquista
import com.developersbeeh.medcontrol.data.repository.AchievementRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val userPreferences: UserPreferences // ✅ DEPENDÊNCIA ADICIONADA
) : ViewModel() {

    private val _user = MutableLiveData<FirebaseUser>()
    val user: LiveData<FirebaseUser> = _user

    private val _updateStatus = MutableLiveData<Event<Result<Unit>>>()
    val updateStatus: LiveData<Event<Result<Unit>>> = _updateStatus

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser != null) {
            _user.value = currentUser
        } else {
            _updateStatus.value = Event(Result.failure(Exception("Usuário não encontrado.")))
        }
    }

    fun saveProfile(
        password: String, confirmPass: String, dataNascimento: String,
        sexo: String, tipoSanguineo: String, peso: String, altura: String,
        condicoes: String, alergias: String
    ) {
        if (password != confirmPass || password.length < 6 || dataNascimento.isBlank()) {
            _updateStatus.value = Event(Result.failure(IllegalArgumentException("Verifique os campos obrigatórios.")))
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val currentUser = _user.value!!
            val result = userRepository.finalizeNewGoogleUser(
                user = currentUser,
                password = password,
                dataNascimento = dataNascimento,
                sexo = sexo,
                tipoSanguineo = tipoSanguineo,
                peso = peso,
                altura = altura,
                condicoes = condicoes,
                alergias = alergias
            )

            if (result.isSuccess) {
                // ✅ LÓGICA DE SINCRONIZAÇÃO ADICIONADA
                userPreferences.saveUserName(currentUser.displayName ?: "")
                userPreferences.saveUserEmail(currentUser.email ?: "")
                userPreferences.saveUserPhotoUrl(currentUser.photoUrl?.toString())

                val fields = listOf(dataNascimento, sexo, tipoSanguineo, peso, altura, condicoes, alergias)
                if (fields.all { it.isNotBlank() }) {
                    val selfCareProfile = userRepository.getSelfCareProfile(_user.value!!.uid)
                    selfCareProfile?.let {
                        val conquista = Conquista(id = TipoConquista.PERFIL_COMPLETO.name, tipo = TipoConquista.PERFIL_COMPLETO)
                        achievementRepository.awardAchievement(it.id, conquista)
                    }
                }
            }

            _updateStatus.postValue(Event(result))
            _isLoading.postValue(false)
        }
    }
}