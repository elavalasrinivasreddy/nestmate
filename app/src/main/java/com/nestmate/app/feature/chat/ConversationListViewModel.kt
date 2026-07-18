package com.nestmate.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Conversation
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.ChatRepository
import com.nestmate.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationListViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val conversations: List<Conversation> = emptyList(),
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUid = authRepository.currentUser?.uid ?: return@launch
            
            combine(
                chatRepository.getConversationsStream(),
                profileRepository.getProfileStream(currentUid)
            ) { convoResult, profileResult ->
                if (convoResult is DataResult.Error) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = convoResult.message) }
                    return@combine
                }
                
                val conversations = (convoResult as? DataResult.Success)?.data ?: emptyList()
                val profile = (profileResult as? DataResult.Success)?.data
                val blockedUids = profile?.blockedUids ?: emptyList()

                val filtered = conversations.filter { convo ->
                    // Omit if ANY participant is blocked by the current user
                    convo.participantUids.none { uid -> uid != currentUid && uid in blockedUids }
                }
                
                _uiState.update { it.copy(isLoading = false, conversations = filtered) }
            }.collect {}
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            profileRepository: ProfileRepository,
            chatRepository: ChatRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ConversationListViewModel(authRepository, profileRepository, chatRepository) as T
            }
    }
}
