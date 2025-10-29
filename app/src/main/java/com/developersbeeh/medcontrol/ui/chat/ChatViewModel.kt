// src/main/java/com/developersbeeh/medcontrol/ui/chat/ChatViewModel.kt
package com.developersbeeh.medcontrol.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.ChatMessage
import com.developersbeeh.medcontrol.data.model.Sender
import com.developersbeeh.medcontrol.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private lateinit var dependentId: String
    private val _uiState = MutableLiveData<ChatUiState>()
    val uiState: LiveData<ChatUiState> = _uiState

    private val currentMessages = mutableListOf<ChatMessage>()
    private var isHistoryLoaded = false

    fun initialize(dependentId: String, dependentName: String) {
        if (this::dependentId.isInitialized && this.dependentId == dependentId) return
        this.dependentId = dependentId
        isHistoryLoaded = false
        loadChatHistory(dependentName)
    }

    private fun loadChatHistory(dependentName: String) {
        _uiState.value = ChatUiState.Loading
        viewModelScope.launch {
            chatRepository.getChatHistory(dependentId).collectLatest { messages ->
                currentMessages.clear()

                if (!isHistoryLoaded && messages.isEmpty()) {
                    val welcomeMessage = ChatMessage(
                        id = "welcome_message",
                        text = "Olá! Sou o Nidus. Agora tenho acesso ao histórico de saúde de $dependentName para respostas mais completas. Como posso ajudar?",
                        sender = Sender.AI.name
                    )
                    currentMessages.add(welcomeMessage)
                }

                currentMessages.addAll(messages)
                isHistoryLoaded = true
                _uiState.postValue(ChatUiState.Success(currentMessages.toList()))
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, sender = Sender.USER.name)
        currentMessages.add(userMessage)

        val typingIndicator = ChatMessage(id = "typing_indicator", sender = Sender.AI.name)
        currentMessages.add(typingIndicator)
        _uiState.value = ChatUiState.Success(currentMessages.toList())

        viewModelScope.launch {
            chatRepository.saveChatMessage(dependentId, userMessage)
            val result = chatRepository.getChatResponse(text, dependentId)

            currentMessages.remove(typingIndicator)

            result.onSuccess { responseText ->
                val aiMessage = ChatMessage(text = responseText, sender = Sender.AI.name)
                currentMessages.add(aiMessage)
                chatRepository.saveChatMessage(dependentId, aiMessage)
            }.onFailure { error ->
                val errorMessage = ChatMessage(
                    text = error.message ?: "Ocorreu um erro. Tente novamente.",
                    sender = Sender.AI.name
                )
                currentMessages.add(errorMessage)
            }
            _uiState.postValue(ChatUiState.Success(currentMessages.toList()))
        }
    }
}