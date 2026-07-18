package com.nestmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Bookmark
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreBookmarkRepository(
    private val firestore: FirebaseFirestore
) : BookmarkRepository {

    private fun userBookmarks(uid: String) = 
        firestore.collection("users").document(uid).collection("bookmarks")

    override fun getBookmarksStream(uid: String): Flow<DataResult<List<Bookmark>>> = callbackFlow {
        val subscription = userBookmarks(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Failed to fetch bookmarks", error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val bookmarks = snapshot.documents.mapNotNull { it.toObject(Bookmark::class.java) }
                        trySend(DataResult.Success(bookmarks))
                    } catch (e: Exception) {
                        trySend(DataResult.Error("Failed to parse bookmarks", e))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun isBookmarkedStream(uid: String, itemId: String): Flow<DataResult<Boolean>> = callbackFlow {
        val subscription = userBookmarks(uid).document(itemId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(DataResult.Error(error.message ?: "Failed to check bookmark status", error))
                return@addSnapshotListener
            }
            trySend(DataResult.Success(snapshot?.exists() == true))
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun addBookmark(uid: String, bookmark: Bookmark): DataResult<Unit> {
        return try {
            userBookmarks(uid).document(bookmark.itemId).set(bookmark).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to add bookmark", e)
        }
    }

    override suspend fun removeBookmark(uid: String, itemId: String): DataResult<Unit> {
        return try {
            userBookmarks(uid).document(itemId).delete().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to remove bookmark", e)
        }
    }
}
