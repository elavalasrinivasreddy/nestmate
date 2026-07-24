package com.nestmate.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Cleanliness
import com.nestmate.app.data.model.DrinkingPreference
import com.nestmate.app.data.model.FoodPreference
import com.nestmate.app.data.model.Lifestyle
import com.nestmate.app.data.model.OccupationType
import com.nestmate.app.data.model.SleepSchedule
import com.nestmate.app.data.model.SmokingPreference
import com.nestmate.app.data.model.UserProfile
import com.nestmate.app.data.model.UserType
import com.nestmate.app.data.model.Verification
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.nestmate.app.data.model.Review
import com.nestmate.app.data.repository.ReviewRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val isSaved: Boolean = false,
        val profile: UserProfile = UserProfile(),
        val reviews: List<Review> = emptyList(),
        val isReviewsLoading: Boolean = true,
        val reviewsError: String? = null,
        val actionSuccessMessage: String? = null
    ) {
        val canSave: Boolean
            get() = profile.displayName.isNotBlank() && !isSaving
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadReviews()
    }

    private fun loadProfile() {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            // Fix (B-001): Use single getProfile() fetch instead of stream to prevent 
            // cache syncs from overwriting the user's active form edits.
            when (val result = profileRepository.getProfile(user.uid)) {
                is DataResult.Success -> {
                    val existingProfile = result.data
                    if (existingProfile != null) {
                        _uiState.update { 
                            it.copy(isLoading = false, profile = existingProfile)
                        }
                    } else {
                        // New profile setup
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                profile = UserProfile(
                                    uid = user.uid,
                                    phoneNumber = user.phoneNumber,
                                    verification = Verification(phoneVerified = user.phoneNumber != null)
                                )
                            )
                        }
                    }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onNameChange(name: String) = updateProfile { it.copy(displayName = name) }
    fun onBioChange(bio: String) = updateProfile { it.copy(bio = bio) }
    fun onUserTypeChange(type: UserType) = updateProfile { it.copy(userType = type) }
    fun onOccupationChange(occ: OccupationType) = updateProfile { it.copy(occupationType = occ) }
    
    fun onSmokingChange(smoking: SmokingPreference) = updateProfile { it.copy(lifestyle = it.lifestyle.copy(smoking = smoking)) }
    fun onDrinkingChange(drinking: DrinkingPreference) = updateProfile { it.copy(lifestyle = it.lifestyle.copy(drinking = drinking)) }
    fun onFoodChange(food: FoodPreference) = updateProfile { it.copy(lifestyle = it.lifestyle.copy(food = food)) }
    fun onSleepChange(sleep: SleepSchedule) = updateProfile { it.copy(lifestyle = it.lifestyle.copy(sleepSchedule = sleep)) }
    fun onCleanlinessChange(clean: Cleanliness) = updateProfile { it.copy(lifestyle = it.lifestyle.copy(cleanliness = clean)) }

    fun addLocation(location: String) {
        if (location.isNotBlank()) {
            updateProfile { 
                val newList = it.preferredLocations.toMutableList().apply { 
                    if (!contains(location)) add(location) 
                }
                it.copy(preferredLocations = newList) 
            }
        }
    }
    
    fun removeLocation(location: String) {
        updateProfile { it.copy(preferredLocations = it.preferredLocations - location) }
    }

    private fun updateProfile(transform: (UserProfile) -> UserProfile) {
        _uiState.update { it.copy(profile = transform(it.profile), isSaved = false) }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (!state.canSave) return
        
        _uiState.update { it.copy(isSaving = true, errorMessage = null, isSaved = false) }
        viewModelScope.launch {
            when (val result = profileRepository.saveProfile(state.profile)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun resetSavedState() {
        _uiState.update { it.copy(isSaved = false) }
    }

    private fun loadReviews() {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            reviewRepository.getReviewsStream(currentUser.uid).collectLatest { result ->
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
                targetId = currentUser.uid, // reviewing the profile they are viewing
                targetType = "USER",
                reviewerUid = currentUser.uid, 
                rating = rating,
                text = text
            )
            val result = reviewRepository.submitReview(review)
            if (result is DataResult.Success) {
                _uiState.update { it.copy(actionSuccessMessage = "Review submitted successfully") }
                loadProfile() // refresh average rating
            } else if (result is DataResult.Error) {
                _uiState.update { it.copy(errorMessage = "Failed to submit review: ${result.message}") }
            }
        }
    }

    fun clearActionMessage() = _uiState.update { it.copy(actionSuccessMessage = null) }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            profileRepository: ProfileRepository,
            reviewRepository: ReviewRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(authRepository, profileRepository, reviewRepository) as T
        }
    }
}
