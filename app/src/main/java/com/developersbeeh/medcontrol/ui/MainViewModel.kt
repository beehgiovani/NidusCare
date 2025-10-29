// src/main/java/com/developersbeeh/medcontrol/ui/MainViewModel.kt
package com.developersbeeh.medcontrol.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow // Importação necessária
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _userProfile = MutableLiveData<Usuario?>()
    val userProfile: LiveData<Usuario?> = _userProfile

    private val _logoutEvent = MutableLiveData<Event<Unit>>()
    val logoutEvent: LiveData<Event<Unit>> = _logoutEvent

    private var profileListenerJob: Job? = null

    fun startListeningToUserProfile() {
        profileListenerJob?.cancel() // Cancela listener anterior para evitar duplicatas
        val uid = auth.currentUser?.uid
        if (uid != null) {
            profileListenerJob = viewModelScope.launch {
                userRepository.listenToUserProfile(uid).collectLatest { user ->
                    _userProfile.postValue(user)
                }
            }
        }
    }


    fun listenToPremiumStatus(): Flow<Boolean>? {
        val uid = auth.currentUser?.uid ?: return null
        return userRepository.getUserStatusFlow(uid)
    }

    fun onLogoutRequest() {
        viewModelScope.launch {
            auth.signOut()
            userPreferences.clear()
            _logoutEvent.postValue(Event(Unit))
        }
    }

    override fun onCleared() {
        super.onCleared()
        profileListenerJob?.cancel()
    }
}