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
import com.nestmate.app.data.repository.TrustRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.nestmate.app.data.model.Review
import com.nestmate.app.data.repository.ReviewRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListingDetailViewModel(
    private val authRepository: AuthRepository,
    private val listingRepository: ListingRepository,
    private val chatRepository: ChatRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val trustRepository: TrustRepository,
    private val reviewRepository: ReviewRepository,
    private val listingId: String
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val listing: Listing? = null,
        val errorMessage: String? = null,
        val isOwner: Boolean = false,
        val isDeleted: Boolean = false,
        val isBookmarked: Boolean = false,
        val conversationIdToLaunch: String? = null,
        val actionSuccessMessage: String? = null,
        val reviews: List<Review> = emptyList(),
        val isReviewsLoading: Boolean = true,
        val reviewsError: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadListing()
        checkBookmarkStatus()
        loadReviews()
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
    
    fun reportUser(reason: String) {
        val ownerUid = _uiState.value.listing?.ownerUid ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = trustRepository.reportUser(ownerUid, reason, ContextType.LISTING, listingId)) {
                is DataResult.Success -> _uiState.update { it.copy(isLoading = false, actionSuccessMessage = "Report submitted.") }
                is DataResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun blockUser() {
        val ownerUid = _uiState.value.listing?.ownerUid ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = trustRepository.blockUser(ownerUid)) {
                is DataResult.Success -> _uiState.update { it.copy(isLoading = false, actionSuccessMessage = "User blocked.") }
                is DataResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            reviewRepository.getReviewsStream(listingId).collectLatest { result ->
                when (result) {
                    is DataResult.Success -> {
                        _uiState.update { it.copy(reviews = result.data, isReviewsLoading = false, reviewsError = null) }
                    }
                    is DataResult.Error -> {
                        _uiState.update { it.copy(isReviewsLoading = false, reviewsError = result.message) }
                    }
                }
            }
        }
    }

    fun submitReview(rating: Float, text: String) {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            val review = Review(
                targetId = listingId,
                targetType = "LISTING",
                reviewerUid = currentUser.uid,
                rating = rating,
                text = text
            )
            val result = reviewRepository.submitReview(review)
            if (result is DataResult.Success) {
                _uiState.update { it.copy(actionSuccessMessage = "Review submitted successfully") }
                // Reload listing to get updated average rating
                loadListing()
            } else if (result is DataResult.Error) {
                _uiState.update { it.copy(errorMessage = "Failed to submit review: ${result.message}") }
            }
        }
    }

    fun clearActionMessage() = _uiState.update { it.copy(actionSuccessMessage = null) }
    fun onChatLaunched() = _uiState.update { it.copy(conversationIdToLaunch = null) }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            listingRepository: ListingRepository,
            chatRepository: ChatRepository,
            bookmarkRepository: BookmarkRepository,
            trustRepository: TrustRepository,
            reviewRepository: ReviewRepository,
            listingId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ListingDetailViewModel(authRepository, listingRepository, chatRepository, bookmarkRepository, trustRepository, reviewRepository, listingId) as T
        }
    }
}
