package com.developersbeeh.medcontrol.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object RoleSelection : SplashDestination()
    object CaregiverDashboard : SplashDestination()
    data class DependentDashboard(val dependentId: String, val dependentName: String) : SplashDestination()
    object CompleteProfile : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _destination = MutableLiveData<Event<SplashDestination>>()
    val destination: LiveData<Event<SplashDestination>> = _destination

    fun decideNextScreen() {
        viewModelScope.launch {
            delay(2000)

            if (!userPreferences.isOnboardingCompleted()) {
                _destination.postValue(Event(SplashDestination.Onboarding))
                return@launch
            }

            val firebaseUser = userRepository.getCurrentUser()
            // Fluxo do Cuidador (usuário autenticado)
            if (firebaseUser != null) {
                val userProfileResult = userRepository.getUserProfile(firebaseUser.uid)
                if (userProfileResult.isSuccess) {
                    val user = userProfileResult.getOrThrow()

                    // Sincroniza os dados do perfil com as preferências locais
                    userPreferences.saveUserName(user.name)
                    userPreferences.saveUserEmail(user.email)
                    userPreferences.saveUserPhotoUrl(user.photoUrl)

                    if (user.profileIncomplete) {
                        _destination.postValue(Event(SplashDestination.CompleteProfile))
                        return@launch
                    }

                    userRepository.syncAndCheckPremiumStatus()
                }

                userPreferences.saveIsCaregiver(true)
                _destination.postValue(Event(SplashDestination.CaregiverDashboard))
                return@launch
            }

            // Fluxo do Dependente (acesso por código)
            val dependentId = userPreferences.getDependentId()
            if (!dependentId.isNullOrBlank()) {
                userPreferences.saveIsCaregiver(false)
                val dependentName = userPreferences.getUserName() // Nome do dependente é salvo aqui
                _destination.postValue(Event(SplashDestination.DependentDashboard(dependentId, dependentName)))
                return@launch
            }

            // Nenhum usuário logado
            _destination.postValue(Event(SplashDestination.RoleSelection))
        }
    }
}