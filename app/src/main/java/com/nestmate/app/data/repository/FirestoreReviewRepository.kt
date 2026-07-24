package com.nestmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Review
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreReviewRepository(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override fun getReviewsStream(targetId: String): Flow<DataResult<List<Review>>> = callbackFlow {
        // No orderBy: whereEqualTo + orderBy needs a composite index (project
        // convention avoids these — see ROADMAP). Sort client-side instead.
        val subscription = firestore.collection("reviews")
            .whereEqualTo("targetId", targetId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Unknown error", error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                        .sortedByDescending { it.createdAt }
                    trySend(DataResult.Success(reviews))
                } else {
                    trySend(DataResult.Success(emptyList()))
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun submitReview(review: Review): DataResult<Unit> {
        return try {
            val reviewRef = if (review.id.isEmpty()) {
                firestore.collection("reviews").document()
            } else {
                firestore.collection("reviews").document(review.id)
            }

            val finalReview = review.copy(id = reviewRef.id)

            firestore.runTransaction { transaction ->
                val targetCollection = if (review.targetType == "LISTING") "listings" else "users"
                val targetRef = firestore.collection(targetCollection).document(review.targetId)
                val snapshot = transaction.get(targetRef)

                if (snapshot.exists()) {
                    val currentCount = snapshot.getLong("reviewCount")?.toInt() ?: 0
                    val currentAvg = snapshot.getDouble("averageRating") ?: 0.0
                    
                    val newCount = currentCount + 1
                    val newAvg = ((currentAvg * currentCount) + finalReview.rating) / newCount

                    transaction.update(targetRef, "reviewCount", newCount)
                    transaction.update(targetRef, "averageRating", newAvg)
                }

                transaction.set(reviewRef, finalReview)
            }.await()

            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to submit review", e)
        }
    }
}
