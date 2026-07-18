package com.nestmate.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.ContextType
import com.nestmate.app.data.model.Conversation
import com.nestmate.app.data.model.Message
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.ChatRepository
import com.nestmate.app.data.repository.TrustRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessageThreadViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val trustRepository: TrustRepository,
    private val conversationId: String
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val conversation: Conversation? = null,
        val messages: List<Message> = emptyList(),
        val currentUserId: String? = null,
        val errorMessage: String? = null,
        val newMessageText: String = "",
        val actionSuccessMessage: String? = null
    ) {
        val canSend: Boolean get() = newMessageText.isNotBlank()
    }

    private val _uiState = MutableStateFlow(UiState(currentUserId = authRepository.currentUser?.uid))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.getConversationStream(conversationId).collectLatest { result ->
                if (result is DataResult.Success) {
                    _uiState.update { it.copy(conversation = result.data) }
                }
            }
        }

        viewModelScope.launch {
            chatRepository.getMessagesStream(conversationId).collectLatest { result ->
                when (result) {
                    is DataResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, messages = result.data) }
                    }
                    is DataResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    fun onMessageChange(text: String) = _uiState.update { it.copy(newMessageText = text) }

    fun sendMessage() {
        val state = _uiState.value
        if (!state.canSend) return

        val text = state.newMessageText
        _uiState.update { it.copy(newMessageText = "") }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(conversationId, text)
            if (result is DataResult.Error) {
                _uiState.update { it.copy(newMessageText = text, errorMessage = result.message) }
            }
        }
    }
    
    fun reportUser(reason: String) {
        val otherUid = _uiState.value.conversation?.participantUids?.firstOrNull { it != _uiState.value.currentUserId } ?: return
        val context = _uiState.value.conversation?.context
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = trustRepository.reportUser(otherUid, reason, context?.type ?: ContextType.LISTING, context?.id)) {
                is DataResult.Success -> _uiState.update { it.copy(isLoading = false, actionSuccessMessage = "Report submitted.") }
                is DataResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun blockUser() {
        val otherUid = _uiState.value.conversation?.participantUids?.firstOrNull { it != _uiState.value.currentUserId } ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = trustRepository.blockUser(otherUid)) {
                is DataResult.Success -> _uiState.update { it.copy(isLoading = false, actionSuccessMessage = "User blocked.") }
                is DataResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun clearActionMessage() = _uiState.update { it.copy(actionSuccessMessage = null) }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            chatRepository: ChatRepository,
            trustRepository: TrustRepository,
            conversationId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MessageThreadViewModel(authRepository, chatRepository, trustRepository, conversationId) as T
        }
    }
}
