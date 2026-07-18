package com.nestmate.app.data.repository

import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Listing
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    /** Gets a stream of active listings, ordered by newest first. */
    fun getActiveListingsStream(): Flow<DataResult<List<Listing>>>

    /** Gets a stream of a single listing by ID. */
    fun getListingStream(listingId: String): Flow<DataResult<Listing?>>

    /** Gets a stream of listings owned by a specific user. */
    fun getListingsByOwnerStream(uid: String): Flow<DataResult<List<Listing>>>

    /** Saves (creates or updates) a listing. */
    suspend fun saveListing(listing: Listing): DataResult<String>

    /** Deletes a listing. */
    suspend fun deleteListing(listingId: String): DataResult<Unit>
}
