package com.nestmate.app.data.model

/**
 * Minimal authenticated-user model exposed to the app, decoupled from
 * Firebase's `FirebaseUser` type.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val phoneNumber: String?,
    val isEmailVerified: Boolean
)
