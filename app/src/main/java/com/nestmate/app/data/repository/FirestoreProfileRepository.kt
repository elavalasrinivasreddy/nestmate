package com.nestmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProfileRepository(
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    private val usersCollection = firestore.collection("users")

    override fun getProfileStream(uid: String): Flow<DataResult<UserProfile?>> = callbackFlow {
        val subscription = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(DataResult.Error(error.message ?: "Failed to fetch profile", error))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    trySend(DataResult.Success(profile))
                } catch (e: Exception) {
                    trySend(DataResult.Error("Failed to parse profile data", e))
                }
            } else {
                // Document doesn't exist
                trySend(DataResult.Success(null))
            }
        }

        awaitClose { subscription.remove() }
    }

    override suspend fun saveProfile(profile: UserProfile): DataResult<Unit> {
        return try {
            val profileWithTimestamp = profile.copy(updatedAt = System.currentTimeMillis())
            usersCollection.document(profile.uid).set(profileWithTimestamp).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to save profile", e)
        }
    }
}
