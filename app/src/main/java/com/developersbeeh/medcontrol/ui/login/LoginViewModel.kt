// src/main/java/com/developersbeeh/medcontrol/ui/login/LoginViewModel.kt
package com.developersbeeh.medcontrol.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.repository.LoginResult
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthStatus {
    AUTHENTICATED,
    UNAUTHENTICATED,
    LOADING,
    ERROR
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authStatus = MutableLiveData<AuthStatus>()
    val authStatus: LiveData<AuthStatus> = _authStatus

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    private val _successEvent = MutableLiveData<Event<String>>()
    val successEvent: LiveData<Event<String>> = _successEvent

    private val _navigateToHomeEvent = MutableLiveData<Event<FirebaseUser>>()
    val navigateToHomeEvent: LiveData<Event<FirebaseUser>> = _navigateToHomeEvent

    private val _navigateToCompleteProfileEvent = MutableLiveData<Event<Unit>>()
    val navigateToCompleteProfileEvent: LiveData<Event<Unit>> = _navigateToCompleteProfileEvent

    init {
        if (auth.currentUser != null) {
            _navigateToHomeEvent.value = Event(auth.currentUser!!)
        } else {
            _authStatus.value = AuthStatus.UNAUTHENTICATED
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorEvent.value = Event("Por favor, insira um e-mail válido.")
            return
        }
        _authStatus.value = AuthStatus.LOADING
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _successEvent.value = Event("E-mail de redefinição enviado!")
                _authStatus.value = AuthStatus.UNAUTHENTICATED
            } else {
                _errorEvent.value = Event("Falha ao enviar e-mail. Verifique o endereço.")
                _authStatus.value = AuthStatus.ERROR
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorEvent.postValue(Event("E-mail e senha são obrigatórios."))
            return
        }
        _authStatus.value = AuthStatus.LOADING
        viewModelScope.launch {
            userRepository.loginWithEmail(email, password).onSuccess { user ->
                _authStatus.value = AuthStatus.AUTHENTICATED
                _navigateToHomeEvent.postValue(Event(user))
            }.onFailure { e ->
                _authStatus.value = AuthStatus.ERROR
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
                    is FirebaseAuthInvalidCredentialsException -> "Senha incorreta."
                    else -> "Erro ao fazer login. Verifique sua conexão."
                }
                _errorEvent.postValue(Event(errorMessage))
            }
        }
    }

    fun firebaseAuthWithGoogle(credential: AuthCredential) {
        signInWithSocial(credential, "Google")
    }

    private fun signInWithSocial(credential: AuthCredential, providerName: String) {
        _authStatus.value = AuthStatus.LOADING
        viewModelScope.launch {
            userRepository.signInWithSocial(credential).onSuccess { loginResult ->
                _authStatus.value = AuthStatus.AUTHENTICATED
                when (loginResult) {
                    is LoginResult.Success -> _navigateToHomeEvent.postValue(Event(loginResult.user))
                    is LoginResult.NewUser -> _navigateToCompleteProfileEvent.postValue(Event(Unit))
                }
            }.onFailure { e ->
                _authStatus.value = AuthStatus.ERROR
                val errorMessage = if (e is FirebaseAuthException && e.errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") {
                    "Já existe uma conta com este e-mail. Tente fazer login com outro método."
                } else { "Falha na autenticação com $providerName: ${e.message}" }
                _errorEvent.postValue(Event(errorMessage))
            }
        }
    }
}