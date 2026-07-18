package com.nestmate.app.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.FirebaseAuthRepository
import com.nestmate.app.data.repository.FirestoreListingRepository
import com.nestmate.app.data.repository.FirestoreProfileRepository
import com.nestmate.app.data.repository.ListingRepository
import com.nestmate.app.data.repository.ProfileRepository

/**
 * Manual dependency container (no Hilt — see docs/DECISIONS.md, ADR-014).
 * Dependencies are added here as features land.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val profileRepository: ProfileRepository
    val listingRepository: ListingRepository
}

class DefaultAppContainer : AppContainer {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(firebaseAuth)
    }

    override val profileRepository: ProfileRepository by lazy {
        FirestoreProfileRepository(firestore)
    }

    override val listingRepository: ListingRepository by lazy {
        FirestoreListingRepository(firestore)
    }
}
