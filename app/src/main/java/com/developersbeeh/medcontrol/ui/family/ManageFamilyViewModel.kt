package com.developersbeeh.medcontrol.ui.family

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Familia
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageFamilyViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _familyDetails = MutableLiveData<Familia?>()
    val familyDetails: LiveData<Familia?> = _familyDetails

    private val _familyMembers = MutableLiveData<List<Usuario>>()
    val familyMembers: LiveData<List<Usuario>> = _familyMembers

    private val _isCurrentUserOwner = MutableLiveData<Boolean>()
    val isCurrentUserOwner: LiveData<Boolean> = _isCurrentUserOwner

    val actionFeedback = MutableLiveData<Event<String>>()

    init {
        loadFamilyData()
    }

    private fun loadFamilyData() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid ?: return@launch
            userRepository.getUserProfile(userId).onSuccess { user ->
                if (!user.familyId.isNullOrBlank()) {
                    val family = userRepository.getFamilyById(user.familyId)
                    _familyDetails.postValue(family)

                    if (family != null) {
                        _isCurrentUserOwner.postValue(family.ownerId == userId)
                        userRepository.getUsersFromIds(family.members).onSuccess { members ->
                            _familyMembers.postValue(members)
                        }
                    }
                }
            }
        }
    }

    fun inviteMember(email: String) {
        val family = _familyDetails.value
        val currentUserId = userRepository.getCurrentUser()?.uid

        if (family == null || currentUserId == null) {
            actionFeedback.postValue(Event("Não foi possível obter os detalhes da família. Tente novamente."))
            return
        }

        viewModelScope.launch {
            val currentUserProfile = userRepository.getUserProfile(currentUserId).getOrNull()
            if (currentUserProfile?.email.equals(email, ignoreCase = true)) {
                actionFeedback.postValue(Event("Você não pode convidar a si mesmo para a família."))
                return@launch
            }

            val findUserResult = userRepository.findUserByEmail(email)
            findUserResult.onSuccess { userToInvite ->
                if (userToInvite == null) {
                    actionFeedback.postValue(Event("Nenhum usuário encontrado com este e-mail."))
                    return@onSuccess
                }

                if (family.members.contains(userToInvite.id)) {
                    actionFeedback.postValue(Event("${userToInvite.name} já é um membro da sua família."))
                    return@onSuccess
                }

                val result = userRepository.inviteFamilyMember(family.id, email)
                result.onSuccess { successMessage ->
                    actionFeedback.postValue(Event(successMessage))
                    loadFamilyData()
                }.onFailure { exception ->
                    actionFeedback.postValue(Event(exception.message ?: "Erro desconhecido ao enviar convite."))
                }

            }.onFailure { exception ->
                actionFeedback.postValue(Event("Erro ao procurar o usuário: ${exception.message}"))
            }
        }
    }

    fun removeMember(member: Usuario) {
        val family = _familyDetails.value ?: return
        viewModelScope.launch {
            val result = userRepository.removeFamilyMember(family.id, member.id)
            result.onSuccess {
                actionFeedback.postValue(Event(it))
                loadFamilyData()
            }.onFailure {
                actionFeedback.postValue(Event(it.message ?: "Erro desconhecido"))
            }
        }
    }
}