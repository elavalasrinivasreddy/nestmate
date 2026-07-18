package com.nestmate.app.data.repository

import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    /**
     * Gets the current user's profile as a stream. Emits null if the profile doesn't exist yet.
     */
    fun getProfileStream(uid: String): Flow<DataResult<UserProfile?>>

    /**
     * Creates or updates the user profile.
     */
    suspend fun saveProfile(profile: UserProfile): DataResult<Unit>
}
