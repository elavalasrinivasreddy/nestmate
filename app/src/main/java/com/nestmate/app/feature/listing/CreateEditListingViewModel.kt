package com.nestmate.app.feature.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.GenderPreference
import com.nestmate.app.data.model.Listing
import com.nestmate.app.data.model.Location
import com.nestmate.app.data.model.RoomType
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateEditListingViewModel(
    private val authRepository: AuthRepository,
    private val listingRepository: ListingRepository,
    private val listingId: String? = null
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val isSaved: Boolean = false,
        val listing: Listing = Listing()
    ) {
        val canSave: Boolean
            get() = listing.title.isNotBlank() &&
                    listing.description.isNotBlank() &&
                    listing.rentAmount > 0 &&
                    listing.location.city.isNotBlank() &&
                    listing.location.area.isNotBlank() &&
                    !isSaving
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        if (listingId != null) {
            loadListing(listingId)
        } else {
            val ownerUid = authRepository.currentUser?.uid ?: ""
            _uiState.update { it.copy(isLoading = false, listing = Listing(ownerUid = ownerUid)) }
        }
    }

    private fun loadListing(id: String) {
        viewModelScope.launch {
            val result = listingRepository.getListingStream(id).first()
            when (result) {
                is DataResult.Success -> {
                    if (result.data != null) {
                        _uiState.update { it.copy(isLoading = false, listing = result.data) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Listing not found") }
                    }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onTitleChange(value: String) = updateListing { it.copy(title = value) }
    fun onDescriptionChange(value: String) = updateListing { it.copy(description = value) }
    fun onRentAmountChange(value: String) {
        val rent = value.toDoubleOrNull() ?: return
        updateListing { it.copy(rentAmount = rent) }
    }
    fun onCityChange(value: String) = updateListing { it.copy(location = it.location.copy(city = value)) }
    fun onAreaChange(value: String) = updateListing { it.copy(location = it.location.copy(area = value)) }
    fun onRoomTypeChange(type: RoomType) = updateListing { it.copy(roomType = type) }
    fun onGenderPreferenceChange(pref: GenderPreference) = updateListing { it.copy(preferences = it.preferences.copy(gender = pref)) }

    private fun updateListing(transform: (Listing) -> Listing) {
        _uiState.update { it.copy(listing = transform(it.listing), errorMessage = null) }
    }

    fun saveListing() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = listingRepository.saveListing(state.listing)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            listingRepository: ListingRepository,
            listingId: String? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CreateEditListingViewModel(authRepository, listingRepository, listingId) as T
        }
    }
}
