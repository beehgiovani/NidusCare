package com.developersbeeh.medcontrol.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userProfile = MutableLiveData<Usuario?>()
    val userProfile: LiveData<Usuario?> = _userProfile

    val premiumStatusText: LiveData<String?> = _userProfile.map { user ->
        val expiryDate = user?.subscriptionExpiryDate?.toDate()
        if (user?.premium == true && expiryDate != null) {
            val dateFormatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
            "Premium ativo até: ${dateFormatter.format(expiryDate)}"
        } else if (user?.premium == true) {
            "Status Premium: Ativo"
        } else {
            null // Retorna nulo para esconder o campo de texto
        }
    }

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    private val _deleteAccountResult = MutableLiveData<Event<Result<Unit>>>()
    val deleteAccountResult: LiveData<Event<Result<Unit>>> = _deleteAccountResult

    fun loadProfileData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val result = userRepository.getUserProfile(userId)
                result.onSuccess {
                    _userProfile.postValue(it)
                }
            }
        }
    }

    fun changePassword(currentPass: String, newPass: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("Usuário não autenticado.")
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
                user.reauthenticate(credential).await()
                user.updatePassword(newPass).await()
                _actionFeedback.postValue(Event("Senha alterada com sucesso!"))
            } catch (e: Exception) {
                _actionFeedback.postValue(Event("Falha ao alterar senha. Verifique sua senha atual."))
            }
        }
    }

    fun reauthenticateAndDeleteAccount(password: String) {
        viewModelScope.launch {
            val result = userRepository.reauthenticateAndDeleteAllData(password)
            _deleteAccountResult.postValue(Event(result))
        }
    }
}