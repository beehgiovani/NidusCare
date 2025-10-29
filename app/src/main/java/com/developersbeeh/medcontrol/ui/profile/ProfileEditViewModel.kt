package com.developersbeeh.medcontrol.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences // ✅ DEPENDÊNCIA ADICIONADA
) : ViewModel() {

    private val _userProfile = MutableLiveData<Usuario>()
    val userProfile: LiveData<Usuario> = _userProfile

    private val _updateStatus = MutableLiveData<Event<Result<Unit>>>()
    val updateStatus: LiveData<Event<Result<Unit>>> = _updateStatus

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _premiumStatusText = MutableLiveData<String?>()
    val premiumStatusText: LiveData<String?> = _premiumStatusText

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> = _errorEvent

    init {
        loadProfile()
    }
    private fun processPremiumStatus(user: Usuario) {
        viewModelScope.launch {
            if (!user.premium) {
                _premiumStatusText.postValue(null)
                return@launch
            }

            if (!user.familyId.isNullOrBlank()) {
                val family = userRepository.getFamilyById(user.familyId)
                val formattedDate = formatExpiryTimestamp(family?.subscriptionExpiryDate)
                _premiumStatusText.postValue(formattedDate)
            } else {
                val formattedDate = formatExpiryTimestamp(user.subscriptionExpiryDate)
                _premiumStatusText.postValue(formattedDate)
            }
        }
    }

    private fun formatExpiryTimestamp(timestamp: Timestamp?): String? {
        if (timestamp == null) return "Status Premium ativo" // Fallback

        return try {
            val instant = Instant.ofEpochSecond(timestamp.seconds)
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            "Premium até ${date.format(formatter)}"
        } catch (e: Exception) {
            "Status Premium ativo"
        }
    }
    private fun loadProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid
            if (userId != null) {
                userRepository.getUserProfile(userId).onSuccess { user ->
                    _userProfile.postValue(user)
                    processPremiumStatus(user)
                }.onFailure { error ->
                    _errorEvent.postValue(Event("Não foi possível carregar o perfil: ${error.message}"))
                }
            } else {
                _errorEvent.postValue(Event("Usuário não autenticado. Por favor, faça o login novamente."))
            }
            _isLoading.postValue(false)
        }
    }

    fun updateProfile(newName: String, newImageUri: Uri?) {
        val currentUser = _userProfile.value
        if (currentUser == null) {
            _updateStatus.value = Event(Result.failure(Exception("Usuário não carregado.")))
            return
        }

        if (newName == currentUser.name && newImageUri == null) {
            _updateStatus.value = Event(Result.success(Unit)) // Nenhuma mudança, sucesso
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            if (newImageUri != null) {
                val uploadResult = userRepository.uploadUserProfilePhoto(currentUser.id, newImageUri)
                uploadResult.onSuccess { photoUrl ->
                    val updatedUser = currentUser.copy(name = newName, photoUrl = photoUrl)
                    val finalResult = userRepository.updateUserProfile(updatedUser)
                    if(finalResult.isSuccess){
                        // ✅ ATUALIZA AS PREFERÊNCIAS LOCAIS
                        userPreferences.saveUserName(updatedUser.name)
                        userPreferences.saveUserPhotoUrl(updatedUser.photoUrl)
                    }
                    _updateStatus.postValue(Event(finalResult))
                }.onFailure { exception ->
                    _updateStatus.postValue(Event(Result.failure(exception)))
                }
            } else {
                val updatedUser = currentUser.copy(name = newName)
                val result = userRepository.updateUserProfile(updatedUser)
                if(result.isSuccess){
                    // ✅ ATUALIZA AS PREFERÊNCIAS LOCAIS
                    userPreferences.saveUserName(updatedUser.name)
                }
                _updateStatus.postValue(Event(result))
            }
            _isLoading.postValue(false)
        }
    }
}