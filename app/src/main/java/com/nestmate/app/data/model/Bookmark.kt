package com.nestmate.app.data.model

data class Bookmark(
    val itemId: String = "",
    val itemType: BookmarkItemType = BookmarkItemType.LISTING,
    val snapshot: BookmarkSnapshot = BookmarkSnapshot(),
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookmarkItemType {
    LISTING, REQUIREMENT
}

data class BookmarkSnapshot(
    val title: String = "",
    val price: Double = 0.0,
    val currency: String = "INR",
    val locationString: String = ""
)
