package com.nestmate.app.core.di

import android.content.Context
import com.nestmate.app.core.settings.SettingsRepository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.BookmarkRepository
import com.nestmate.app.data.repository.ChatRepository
import com.nestmate.app.data.repository.FirebaseAuthRepository
import com.nestmate.app.data.repository.FirestoreBookmarkRepository
import com.nestmate.app.data.repository.FirestoreChatRepository
import com.nestmate.app.data.repository.FirestoreListingRepository
import com.nestmate.app.data.repository.FirestoreProfileRepository
import com.nestmate.app.data.repository.FirestoreRequirementRepository
import com.nestmate.app.data.repository.FirestoreTrustRepository
import com.nestmate.app.data.repository.FirestoreReviewRepository
import com.nestmate.app.data.repository.ListingRepository
import com.nestmate.app.data.repository.ProfileRepository
import com.nestmate.app.data.repository.AIAssistantRepository

import com.nestmate.app.data.repository.RequirementRepository
import com.nestmate.app.data.repository.TrustRepository
import com.nestmate.app.data.repository.ReviewRepository

/**
 * Manual dependency container (no Hilt — see docs/DECISIONS.md, ADR-014).
 * Dependencies are added here as features land.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val profileRepository: ProfileRepository
    val listingRepository: ListingRepository
    val requirementRepository: RequirementRepository
    val chatRepository: ChatRepository
    val bookmarkRepository: BookmarkRepository
    val trustRepository: TrustRepository
    val reviewRepository: ReviewRepository
    val aiAssistantRepository: AIAssistantRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

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

    override val requirementRepository: RequirementRepository by lazy {
        FirestoreRequirementRepository(firestore)
    }

    override val chatRepository: ChatRepository by lazy {
        FirestoreChatRepository(firestore, authRepository, profileRepository)
    }

    override val bookmarkRepository: BookmarkRepository by lazy {
        FirestoreBookmarkRepository(firestore)
    }

    override val trustRepository: TrustRepository by lazy {
        FirestoreTrustRepository(firestore, authRepository)
    }

    override val reviewRepository: ReviewRepository by lazy {
        FirestoreReviewRepository(firestore)
    }

    override val aiAssistantRepository: AIAssistantRepository by lazy {
        AIAssistantRepository("TODO_ADD_API_KEY")
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext)
    }
}
