package com.nestmate.app.data.model

/**
 * Minimal authenticated-user model exposed to the app, decoupled from
 * Firebase's `FirebaseUser` type.
 */
data class AuthUser(
    val uid: String,
    val phoneNumber: String?,
    val email: String? = null // Optional, kept in case users link it later or for legacy
)
