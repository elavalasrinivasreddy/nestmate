package com.nestmate.app.data.repository

import android.app.Activity
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Authentication boundary. The UI/ViewModels depend on this interface, never on
 * Firebase directly (see docs/ARCHITECTURE.md).
 */
interface AuthRepository {

    /** The currently signed-in user, or null. */
    val currentUser: AuthUser?

    /** Emits the current user on every auth-state change (sign-in / sign-out). */
    fun authState(): Flow<AuthUser?>

    /**
     * Starts phone-number verification for the given [phoneNumber] (E.164, e.g.
     * `+919876543210`). Signs in with the phone credential.
     *
     * Exactly one of [onCodeSent] or [onVerified] fires first: SMS auto-retrieval
     * (or a Firebase test number configured for instant validation) resolves
     * straight to [onVerified]; otherwise [onCodeSent] hands back a
     * `verificationId` for [confirmPhoneCode]. [onVerificationFailed] fires on
     * invalid numbers, quota limits, etc. Callback-based — Firebase's
     * `PhoneAuthProvider` has no coroutine-friendly API and needs the hosting
     * [activity] for its reCAPTCHA fallback.
     */
    fun startPhoneVerification(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationFailed: (message: String) -> Unit,
        onVerified: (DataResult<AuthUser>) -> Unit
    )

    /** Confirms a manually-entered SMS code against a `verificationId` from [startPhoneVerification]. */
    fun confirmPhoneCode(
        verificationId: String,
        code: String,
        onVerified: (DataResult<AuthUser>) -> Unit
    )

    fun signOut()
}
