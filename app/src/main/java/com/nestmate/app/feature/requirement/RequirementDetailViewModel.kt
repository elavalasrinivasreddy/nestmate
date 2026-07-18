package com.nestmate.app.feature.requirement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Bookmark
import com.nestmate.app.data.model.BookmarkItemType
import com.nestmate.app.data.model.BookmarkSnapshot
import com.nestmate.app.data.model.ContextType
import com.nestmate.app.data.model.Requirement
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.BookmarkRepository
import com.nestmate.app.data.repository.ChatRepository
import com.nestmate.app.data.repository.RequirementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RequirementDetailViewModel(
    private val authRepository: AuthRepository,
    private val requirementRepository: RequirementRepository,
    private val chatRepository: ChatRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val requirementId: String
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val requirement: Requirement? = null,
        val errorMessage: String? = null,
        val isOwner: Boolean = false,
        val isDeleted: Boolean = false,
        val isBookmarked: Boolean = false,
        val conversationIdToLaunch: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadRequirement()
        checkBookmarkStatus()
    }

    private fun loadRequirement() {
        viewModelScope.launch {
            requirementRepository.getRequirementStream(requirementId).collectLatest { result ->
                when (result) {
                    is DataResult.Success -> {
                        val isOwner = result.data?.seekerUid == authRepository.currentUser?.uid
                        _uiState.update { it.copy(isLoading = false, requirement = result.data, isOwner = isOwner) }
                    }
                    is DataResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    private fun checkBookmarkStatus() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            bookmarkRepository.isBookmarkedStream(uid, requirementId).collectLatest { result ->
                if (result is DataResult.Success) {
                    _uiState.update { it.copy(isBookmarked = result.data) }
                }
            }
        }
    }

    fun toggleBookmark() {
        val uid = authRepository.currentUser?.uid ?: return
        val req = _uiState.value.requirement ?: return
        val currentlyBookmarked = _uiState.value.isBookmarked

        viewModelScope.launch {
            if (currentlyBookmarked) {
                bookmarkRepository.removeBookmark(uid, requirementId)
            } else {
                val bookmark = Bookmark(
                    itemId = requirementId,
                    itemType = BookmarkItemType.REQUIREMENT,
                    snapshot = BookmarkSnapshot(
                        title = req.title,
                        price = req.budgetMax,
                        currency = req.currency,
                        locationString = req.preferredLocations.firstOrNull() ?: "Multiple Locations"
                    )
                )
                bookmarkRepository.addBookmark(uid, bookmark)
            }
        }
    }

    fun deleteRequirement() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = requirementRepository.deleteRequirement(requirementId)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun startChat() {
        val ownerUid = _uiState.value.requirement?.seekerUid ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = chatRepository.createOrGetConversation(ownerUid, ContextType.REQUIREMENT, requirementId)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, conversationIdToLaunch = result.data) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onChatLaunched() {
        _uiState.update { it.copy(conversationIdToLaunch = null) }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            requirementRepository: RequirementRepository,
            chatRepository: ChatRepository,
            bookmarkRepository: BookmarkRepository,
            requirementId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RequirementDetailViewModel(authRepository, requirementRepository, chatRepository, bookmarkRepository, requirementId) as T
        }
    }
}
