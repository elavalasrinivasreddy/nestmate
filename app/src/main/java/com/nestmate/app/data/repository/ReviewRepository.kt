package com.nestmate.app.data.repository

import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    /** Gets a stream of reviews for a specific target (Listing or UserProfile), ordered by newest first. */
    fun getReviewsStream(targetId: String): Flow<DataResult<List<Review>>>

    /** Submits a new review. Updates the target's averageRating and reviewCount inside a transaction. */
    suspend fun submitReview(review: Review): DataResult<Unit>
}
