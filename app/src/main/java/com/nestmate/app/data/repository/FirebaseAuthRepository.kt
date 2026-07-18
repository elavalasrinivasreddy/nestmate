package com.nestmate.app.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.AuthUser
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Firebase-backed [AuthRepository]. Keeps all Firebase Auth calls in one place
 * and maps results into [DataResult] / [AuthUser].
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = auth.currentUser?.toAuthUser()

    override fun authState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAuthUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun startPhoneVerification(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationFailed: (message: String) -> Unit,
        onVerified: (DataResult<AuthUser>) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                resolvePhoneCredential(credential, onVerified)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onVerificationFailed(e.message ?: "Could not verify this phone number.")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                onCodeSent(verificationId)
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun confirmPhoneCode(
        verificationId: String,
        code: String,
        onVerified: (DataResult<AuthUser>) -> Unit
    ) {
        resolvePhoneCredential(PhoneAuthProvider.getCredential(verificationId, code), onVerified)
    }

    /**
     * Signs in with the given [credential]. Firebase's phone APIs
     * are Task-based rather than suspend, so this stays callback-based to match.
     */
    private fun resolvePhoneCredential(
        credential: PhoneAuthCredential,
        onResult: (DataResult<AuthUser>) -> Unit
    ) {
        val task = auth.signInWithCredential(credential)
        task.addOnCompleteListener { completed ->
            if (completed.isSuccessful) {
                val user = completed.result?.user?.toAuthUser()
                onResult(
                    user?.let { DataResult.Success(it) }
                        ?: DataResult.Error("Phone verified, but no user was returned.")
                )
            } else {
                onResult(DataResult.Error(completed.exception?.message ?: "Could not verify your phone.", completed.exception))
            }
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
    uid = uid,
    phoneNumber = phoneNumber,
    email = email
)
