package com.nestmate.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Conversation
import com.nestmate.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationListViewModel(
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
            chatRepository.getConversationsStream().collectLatest { result ->
                when (result) {
                    is DataResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, conversations = result.data) }
                    }
                    is DataResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(chatRepository: ChatRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ConversationListViewModel(chatRepository) as T
            }
    }
}
