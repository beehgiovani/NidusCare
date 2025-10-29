package com.developersbeeh.medcontrol.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // ✅ CORREÇÃO: ViewModel agora usa um listener em tempo real
    private val _userProfile: LiveData<Usuario?> = userRepository
        .listenToUserProfile(auth.currentUser?.uid ?: "")
        .asLiveData()

    val userProfile: LiveData<Usuario?> = _userProfile

    private val _navigateToLoginEvent = MutableLiveData<Event<Unit>>()
    val navigateToLoginEvent: LiveData<Event<Unit>> = _navigateToLoginEvent

    // A função 'loadUserProfile' não é mais necessária, pois o Flow faz isso automaticamente.

    fun logout() {
        auth.signOut()
        userPreferences.clear()
        _navigateToLoginEvent.postValue(Event(Unit))
    }
}