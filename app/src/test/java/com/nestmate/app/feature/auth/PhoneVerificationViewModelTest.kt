package com.nestmate.app.feature.auth

import android.app.Activity
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.AuthUser
import com.nestmate.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Never invoked — the fake repository ignores it, so no real Activity behavior is needed. */
private class NoOpActivity : Activity()

/**
 * A scriptable [AuthRepository] fake — no Firebase/coroutine-test deps needed
 * since [PhoneVerificationViewModel] drives the callback-based phone API
 * synchronously (see docs/ARCHITECTURE.md → repositories are fakeable).
 */
private class FakeAuthRepository : AuthRepository {
    override var currentUser: AuthUser? = null
    var onStartVerification: ((phoneNumber: String) -> Unit)? = null
    var onConfirmCode: ((verificationId: String, code: String) -> Unit)? = null

    override fun authState(): Flow<AuthUser?> = MutableSharedFlow()

    override suspend fun signUpWithEmail(email: String, password: String): DataResult<AuthUser> =
        error("not used in this test")

    override suspend fun signInWithEmail(email: String, password: String): DataResult<AuthUser> =
        error("not used in this test")

    override fun startPhoneVerification(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationFailed: (message: String) -> Unit,
        onVerified: (DataResult<AuthUser>) -> Unit
    ) {
        lastOnCodeSent = onCodeSent
        lastOnVerificationFailed = onVerificationFailed
        lastOnVerified = onVerified
        onStartVerification?.invoke(phoneNumber)
    }

    override fun confirmPhoneCode(
        verificationId: String,
        code: String,
        onVerified: (DataResult<AuthUser>) -> Unit
    ) {
        lastOnVerified = onVerified
        onConfirmCode?.invoke(verificationId, code)
    }

    override fun signOut() {}

    var lastOnCodeSent: ((String) -> Unit)? = null
    var lastOnVerificationFailed: ((String) -> Unit)? = null
    var lastOnVerified: ((DataResult<AuthUser>) -> Unit)? = null
}

class PhoneVerificationViewModelTest {

    private val activity = NoOpActivity()
    private val testUser = AuthUser(uid = "u1", email = "a@b.com", phoneNumber = "+911234567890", isEmailVerified = true)

    @Test
    fun `canSendCode requires E164-ish number and rejects while loading`() {
        val repo = FakeAuthRepository()
        val vm = PhoneVerificationViewModel(repo)

        assertFalse(vm.uiState.value.canSendCode) // blank number
        vm.onPhoneNumberChange("+9198765")
        assertTrue(vm.uiState.value.canSendCode)
        vm.onPhoneNumberChange("9876543210") // missing '+'
        assertFalse(vm.uiState.value.canSendCode)
    }

    @Test
    fun `sendCode moves to ENTER_CODE step when repository reports code sent`() {
        val repo = FakeAuthRepository().apply {
            onStartVerification = { lastOnCodeSent?.invoke("verification-id-1") }
        }
        val vm = PhoneVerificationViewModel(repo)
        vm.onPhoneNumberChange("+919876543210")

        vm.sendCode(activity)

        val state = vm.uiState.value
        assertEquals(PhoneVerificationViewModel.Step.ENTER_CODE, state.step)
        assertEquals("verification-id-1", state.verificationId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `sendCode that auto-verifies marks the state verified without a code step`() {
        val repo = FakeAuthRepository().apply {
            onStartVerification = { lastOnVerified?.invoke(DataResult.Success(testUser)) }
        }
        val vm = PhoneVerificationViewModel(repo)
        vm.onPhoneNumberChange("+919876543210")

        vm.sendCode(activity)

        val state = vm.uiState.value
        assertTrue(state.isVerified)
        assertEquals(PhoneVerificationViewModel.Step.ENTER_PHONE, state.step) // never advanced — no code step needed
    }

    @Test
    fun `confirmCode surfaces a repository error without marking verified`() {
        val repo = FakeAuthRepository().apply {
            onStartVerification = { lastOnCodeSent?.invoke("verification-id-2") }
            onConfirmCode = { _, _ -> lastOnVerified?.invoke(DataResult.Error("Invalid code.")) }
        }
        val vm = PhoneVerificationViewModel(repo)
        vm.onPhoneNumberChange("+919876543210")
        vm.sendCode(activity)
        vm.onCodeChange("000000")

        vm.confirmCode()

        val state = vm.uiState.value
        assertFalse(state.isVerified)
        assertEquals("Invalid code.", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `confirmCode succeeds and marks the state verified`() {
        val repo = FakeAuthRepository().apply {
            onStartVerification = { lastOnCodeSent?.invoke("verification-id-3") }
            onConfirmCode = { _, _ -> lastOnVerified?.invoke(DataResult.Success(testUser)) }
        }
        val vm = PhoneVerificationViewModel(repo)
        vm.onPhoneNumberChange("+919876543210")
        vm.sendCode(activity)
        vm.onCodeChange("123456")

        vm.confirmCode()

        val state = vm.uiState.value
        assertTrue(state.isVerified)
        assertNull(state.errorMessage)
    }
}
