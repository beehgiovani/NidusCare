package com.developersbeeh.medcontrol.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Convite
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.Event
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ManageCaregiversViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _selectedDependente = MutableLiveData<Dependente?>()
    val selectedDependente: LiveData<Dependente?> = _selectedDependente

    val isInviteOnlyMode: LiveData<Boolean> = _selectedDependente.map { it == null }

    val currentCaregivers: LiveData<List<Usuario>> = _selectedDependente.switchMap { dependente ->
        if (dependente == null) {
            MutableLiveData(emptyList())
        } else {
            firestoreRepository.listenToDependentProfile(dependente.id)
                .flatMapLatest { updatedDependent ->
                    flow {
                        if (updatedDependent != null && updatedDependent.cuidadorIds.isNotEmpty()) {
                            userRepository.getUsersFromIds(updatedDependent.cuidadorIds).onSuccess { users ->
                                emit(users)
                            }.onFailure {
                                emit(emptyList())
                            }
                        } else {
                            emit(emptyList())
                        }
                    }
                }.asLiveData()
        }
    }

    val pendingInvites: LiveData<List<Convite>> = _selectedDependente.switchMap { dependente ->
        if (dependente == null) {
            MutableLiveData(emptyList())
        } else {
            userRepository.getPendingInvitesForDependent(dependente.id).asLiveData()
        }
    }

    private val _receivedInvites = MutableLiveData<List<Convite>>()
    val receivedInvites: LiveData<List<Convite>> = _receivedInvites

    private val _actionFeedback = MutableLiveData<Event<String>>()
    val actionFeedback: LiveData<Event<String>> = _actionFeedback

    fun initialize(dependente: Dependente?) {
        if (_selectedDependente.value?.id != dependente?.id) {
            _selectedDependente.value = dependente
        }
        if (dependente == null) {
            loadReceivedInvites()
        }
    }

    fun loadReceivedInvites() {
        viewModelScope.launch {
            val email = userRepository.getCurrentUser()?.email
            if (email != null) {
                userRepository.getReceivedInvites(email).collect { invites ->
                    _receivedInvites.postValue(invites)
                }
            } else {
                _receivedInvites.postValue(emptyList())
            }
        }
    }

    fun inviteCaregiver(email: String) {
        val dependente = selectedDependente.value
        if (dependente == null) {
            _actionFeedback.value = Event("Erro: Nenhum dependente selecionado para convidar.")
            return
        }

        // ✅ ADIÇÃO: Validação para impedir o compartilhamento de perfis de autocuidado.
        if (dependente.isSelfCareProfile) {
            _actionFeedback.value = Event("Perfis de autocuidado não podem ser compartilhados.")
            return
        }

        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "dependenteId" to dependente.id,
                    "email" to email
                )
                Firebase.functions
                    .getHttpsCallable("inviteCaregiverByEmail")
                    .call(data)
                    .await()
                _actionFeedback.postValue(Event("Convite enviado com sucesso!"))
            } catch (e: Exception) {
                if (e is FirebaseFunctionsException) {
                    _actionFeedback.postValue(Event("Erro: ${e.details}"))
                } else {
                    _actionFeedback.postValue(Event("Erro ao enviar convite: ${e.message}"))
                }
            }
        }
    }

    fun removeCaregiver(caregiverToRemove: Usuario) {
        val dependente = selectedDependente.value
        if (dependente == null) {
            _actionFeedback.value = Event("Erro: Nenhum dependente selecionado.")
            return
        }
        if (dependente.cuidadorIds.size <= 1) {
            _actionFeedback.value = Event("Não é possível remover o único cuidador.")
            return
        }
        viewModelScope.launch {
            userRepository.removeCaregiver(dependente.id, caregiverToRemove.id).onSuccess {
                _actionFeedback.postValue(Event("${caregiverToRemove.name} foi removido."))
            }.onFailure {
                _actionFeedback.postValue(Event("Erro ao remover cuidador."))
            }
        }
    }

    fun cancelInvite(invite: Convite) {
        viewModelScope.launch {
            userRepository.cancelInvite(invite.id).onSuccess {
                _actionFeedback.postValue(Event("Convite cancelado/recusado."))
            }.onFailure {
                _actionFeedback.postValue(Event("Erro ao cancelar convite."))
            }
        }
    }

    fun acceptInvite(convite: Convite) {
        viewModelScope.launch {
            userRepository.acceptInvite(convite).onSuccess {
                _actionFeedback.postValue(Event("Convite aceito com sucesso!"))
            }.onFailure {
                _actionFeedback.postValue(Event("Erro ao aceitar convite: ${it.message}"))
            }
        }
    }
}