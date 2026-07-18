package com.nestmate.app.data.repository

import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    /** Gets a stream of all bookmarks for a user, ordered by newest first. */
    fun getBookmarksStream(uid: String): Flow<DataResult<List<Bookmark>>>

    /** Checks if a specific item is bookmarked by the user. */
    fun isBookmarkedStream(uid: String, itemId: String): Flow<DataResult<Boolean>>

    /** Adds a bookmark. */
    suspend fun addBookmark(uid: String, bookmark: Bookmark): DataResult<Unit>

    /** Removes a bookmark. */
    suspend fun removeBookmark(uid: String, itemId: String): DataResult<Unit>
}
