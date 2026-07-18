package com.nestmate.app.core.di

import com.google.firebase.auth.FirebaseAuth
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.FirebaseAuthRepository

/**
 * Manual dependency container (no Hilt — see docs/DECISIONS.md, ADR-014).
 * Dependencies are added here as features land.
 */
interface AppContainer {
    val authRepository: AuthRepository
}

class DefaultAppContainer : AppContainer {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(firebaseAuth)
    }
}
