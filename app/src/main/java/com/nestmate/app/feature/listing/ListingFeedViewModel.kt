package com.nestmate.app.feature.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Listing
import com.nestmate.app.data.model.RoomType
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.ListingRepository
import com.nestmate.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListingFeedViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val listingRepository: ListingRepository
) : ViewModel() {

    data class FilterState(
        val city: String = "",
        val roomType: RoomType? = null,
        val maxRent: String = ""
    )

    data class UiState(
        val isLoading: Boolean = true,
        val listings: List<Listing> = emptyList(),
        val filters: FilterState = FilterState(),
        val isFilterSheetVisible: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val filterStateFlow = MutableStateFlow(FilterState())

    init {
        viewModelScope.launch {
            val currentUid = authRepository.currentUser?.uid ?: return@launch
            
            combine(
                listingRepository.getActiveListingsStream(),
                profileRepository.getProfileStream(currentUid),
                filterStateFlow
            ) { listingResult, profileResult, filters ->
                if (listingResult is DataResult.Error) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = listingResult.message) }
                    return@combine
                }
                
                val listings = (listingResult as? DataResult.Success)?.data ?: emptyList()
                val profile = (profileResult as? DataResult.Success)?.data
                val blockedUids = profile?.blockedUids ?: emptyList()

                val filtered = listings.filter { listing ->
                    // 1. Trust filtering: omit blocked users
                    if (listing.ownerUid in blockedUids) return@filter false
                    
                    // 2. Discovery filtering
                    val q = filters.city.trim()
                    val matchCity = q.isBlank() ||
                        listing.location.city.contains(q, ignoreCase = true) ||
                        listing.location.area.contains(q, ignoreCase = true)
                    val matchRoomType = filters.roomType == null || listing.roomType == filters.roomType
                    val matchRent = filters.maxRent.toDoubleOrNull()?.let { max -> listing.rentAmount <= max } ?: true
                    matchCity && matchRoomType && matchRent
                }
                _uiState.update { it.copy(isLoading = false, listings = filtered, filters = filters, errorMessage = null) }
            }.collect {}
        }
    }

    fun showFilterSheet() = _uiState.update { it.copy(isFilterSheetVisible = true) }
    fun hideFilterSheet() = _uiState.update { it.copy(isFilterSheetVisible = false) }

    fun applyFilters(city: String, roomType: RoomType?, maxRent: String) {
        filterStateFlow.value = FilterState(city, roomType, maxRent)
        hideFilterSheet()
    }

    fun clearFilters() {
        filterStateFlow.value = FilterState()
        hideFilterSheet()
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            profileRepository: ProfileRepository,
            listingRepository: ListingRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ListingFeedViewModel(authRepository, profileRepository, listingRepository) as T
        }
    }
}
