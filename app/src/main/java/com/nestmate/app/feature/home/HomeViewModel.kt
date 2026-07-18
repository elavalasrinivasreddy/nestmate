package com.nestmate.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.UserProfile
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val profile: UserProfile? = null,
        val errorMessage: String? = null
    ) {
        val hasProfile: Boolean get() = profile != null
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val uid = authRepository.currentUser?.uid ?: return
        
        viewModelScope.launch {
            profileRepository.getProfileStream(uid).collectLatest { result ->
                when (result) {
                    is DataResult.Success -> {
                        _uiState.update { 
                            it.copy(isLoading = false, profile = result.data)
                        }
                    }
                    is DataResult.Error -> {
                        _uiState.update { 
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            profileRepository: ProfileRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(authRepository, profileRepository) as T
        }
    }
}
