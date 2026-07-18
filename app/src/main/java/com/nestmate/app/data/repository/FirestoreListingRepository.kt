package com.nestmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Listing
import com.nestmate.app.data.model.ListingStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreListingRepository(
    private val firestore: FirebaseFirestore
) : ListingRepository {

    private val listingsCollection = firestore.collection("listings")

    override fun getActiveListingsStream(): Flow<DataResult<List<Listing>>> = callbackFlow {
        val subscription = listingsCollection
            .whereEqualTo("status", ListingStatus.ACTIVE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Failed to fetch listings", error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val listings = snapshot.documents.mapNotNull { it.toObject(Listing::class.java) }
                        trySend(DataResult.Success(listings))
                    } catch (e: Exception) {
                        trySend(DataResult.Error("Failed to parse listings", e))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getListingStream(listingId: String): Flow<DataResult<Listing?>> = callbackFlow {
        val subscription = listingsCollection.document(listingId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(DataResult.Error(error.message ?: "Failed to fetch listing", error))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                try {
                    trySend(DataResult.Success(snapshot.toObject(Listing::class.java)))
                } catch (e: Exception) {
                    trySend(DataResult.Error("Failed to parse listing", e))
                }
            } else {
                trySend(DataResult.Success(null))
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getListingsByOwnerStream(uid: String): Flow<DataResult<List<Listing>>> = callbackFlow {
        val subscription = listingsCollection
            .whereEqualTo("ownerUid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Failed to fetch your listings", error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val listings = snapshot.documents.mapNotNull { it.toObject(Listing::class.java) }
                        trySend(DataResult.Success(listings))
                    } catch (e: Exception) {
                        trySend(DataResult.Error("Failed to parse your listings", e))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveListing(listing: Listing): DataResult<String> {
        return try {
            val isNew = listing.id.isEmpty()
            val docRef = if (isNew) listingsCollection.document() else listingsCollection.document(listing.id)
            
            val listingToSave = listing.copy(
                id = docRef.id,
                updatedAt = System.currentTimeMillis(),
                createdAt = if (isNew) System.currentTimeMillis() else listing.createdAt
            )
            
            docRef.set(listingToSave).await()
            DataResult.Success(docRef.id)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to save listing", e)
        }
    }

    override suspend fun deleteListing(listingId: String): DataResult<Unit> {
        return try {
            listingsCollection.document(listingId).delete().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to delete listing", e)
        }
    }
}
