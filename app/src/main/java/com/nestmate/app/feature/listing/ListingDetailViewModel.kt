package com.nestmate.app.feature.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Bookmark
import com.nestmate.app.data.model.BookmarkItemType
import com.nestmate.app.data.model.BookmarkSnapshot
import com.nestmate.app.data.model.ContextType
import com.nestmate.app.data.model.Listing
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.BookmarkRepository
import com.nestmate.app.data.repository.ChatRepository
import com.nestmate.app.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListingDetailViewModel(
    private val authRepository: AuthRepository,
    private val listingRepository: ListingRepository,
    private val chatRepository: ChatRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val listingId: String
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val listing: Listing? = null,
        val errorMessage: String? = null,
        val isOwner: Boolean = false,
        val isDeleted: Boolean = false,
        val isBookmarked: Boolean = false,
        val conversationIdToLaunch: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadListing()
        checkBookmarkStatus()
    }

    private fun loadListing() {
        viewModelScope.launch {
            listingRepository.getListingStream(listingId).collectLatest { result ->
                when (result) {
                    is DataResult.Success -> {
                        val isOwner = result.data?.ownerUid == authRepository.currentUser?.uid
                        _uiState.update { it.copy(isLoading = false, listing = result.data, isOwner = isOwner) }
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
            bookmarkRepository.isBookmarkedStream(uid, listingId).collectLatest { result ->
                if (result is DataResult.Success) {
                    _uiState.update { it.copy(isBookmarked = result.data) }
                }
            }
        }
    }

    fun toggleBookmark() {
        val uid = authRepository.currentUser?.uid ?: return
        val listing = _uiState.value.listing ?: return
        val currentlyBookmarked = _uiState.value.isBookmarked

        viewModelScope.launch {
            if (currentlyBookmarked) {
                bookmarkRepository.removeBookmark(uid, listingId)
            } else {
                val bookmark = Bookmark(
                    itemId = listingId,
                    itemType = BookmarkItemType.LISTING,
                    snapshot = BookmarkSnapshot(
                        title = listing.title,
                        price = listing.rentAmount,
                        currency = listing.currency,
                        locationString = "${listing.location.area}, ${listing.location.city}"
                    )
                )
                bookmarkRepository.addBookmark(uid, bookmark)
            }
        }
    }

    fun deleteListing() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = listingRepository.deleteListing(listingId)) {
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
        val ownerUid = _uiState.value.listing?.ownerUid ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = chatRepository.createOrGetConversation(ownerUid, ContextType.LISTING, listingId)) {
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
            listingRepository: ListingRepository,
            chatRepository: ChatRepository,
            bookmarkRepository: BookmarkRepository,
            listingId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ListingDetailViewModel(authRepository, listingRepository, chatRepository, bookmarkRepository, listingId) as T
        }
    }
}
