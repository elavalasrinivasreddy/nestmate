package com.nestmate.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val email: String = "",
        val password: String = "",
        val isSignUp: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isAuthenticated: Boolean = false,
    ) {
        val canSubmit: Boolean
            get() = email.isNotBlank() && password.length >= MIN_PASSWORD_LENGTH && !isLoading
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun toggleMode() = _uiState.update { it.copy(isSignUp = !it.isSignUp, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (state.isSignUp) {
                authRepository.signUpWithEmail(state.email, state.password)
            } else {
                authRepository.signInWithEmail(state.email, state.password)
            }
            when (result) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                is DataResult.Error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6

        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(authRepository) as T
            }
    }
}
