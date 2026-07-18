package com.nestmate.app.feature.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.AuthUser
import com.nestmate.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Phone-number verification, reached from Home once a user already has an
 * email/password account (ADR-018 — 2b). Links the phone credential to the
 * signed-in [AuthRepository.currentUser]; there is no separate account.
 */
class PhoneVerificationViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    enum class Step { ENTER_PHONE, ENTER_CODE }

    data class UiState(
        val phoneNumber: String = "",
        val code: String = "",
        val step: Step = Step.ENTER_PHONE,
        val verificationId: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isVerified: Boolean = false,
    ) {
        val canSendCode: Boolean
            get() = phoneNumber.trim().let { it.startsWith("+") && it.length >= MIN_PHONE_LENGTH } && !isLoading

        val canConfirmCode: Boolean
            get() = code.trim().length >= MIN_CODE_LENGTH && !isLoading
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onPhoneNumberChange(value: String) = _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }

    fun onCodeChange(value: String) = _uiState.update { it.copy(code = value, errorMessage = null) }

    fun sendCode(activity: Activity) {
        val state = _uiState.value
        if (!state.canSendCode) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        authRepository.startPhoneVerification(
            phoneNumber = state.phoneNumber.trim(),
            activity = activity,
            onCodeSent = { verificationId ->
                _uiState.update {
                    it.copy(isLoading = false, step = Step.ENTER_CODE, verificationId = verificationId, code = "")
                }
            },
            onVerificationFailed = { message ->
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            },
            onVerified = ::applyResult
        )
    }

    fun confirmCode() {
        val state = _uiState.value
        val verificationId = state.verificationId ?: return
        if (!state.canConfirmCode) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        authRepository.confirmPhoneCode(verificationId, state.code.trim(), ::applyResult)
    }

    private fun applyResult(result: DataResult<AuthUser>) {
        when (result) {
            is DataResult.Success -> _uiState.update { it.copy(isLoading = false, isVerified = true) }
            is DataResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
    }

    companion object {
        const val MIN_PHONE_LENGTH = 8
        const val MIN_CODE_LENGTH = 6

        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PhoneVerificationViewModel(authRepository) as T
            }
    }
}
