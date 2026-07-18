package com.nestmate.app.data.model

data class Listing(
    val id: String = "",
    val ownerUid: String = "",
    val title: String = "",
    val description: String = "",
    val roomType: RoomType = RoomType.PRIVATE,
    val rentAmount: Double = 0.0,
    val currency: String = "INR",
    val depositAmount: Double? = null,
    val location: Location = Location(),
    val availableFrom: Long = System.currentTimeMillis(),
    val preferences: RoommatePreferences = RoommatePreferences(),
    val status: ListingStatus = ListingStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RoomType {
    PRIVATE, SHARED, ENTIRE
}

data class Location(
    val city: String = "",
    val area: String = ""
)

data class RoommatePreferences(
    val gender: GenderPreference = GenderPreference.ANY,
    val occupationType: OccupationType? = null, // null means any
    val smoking: SmokingPreference? = null, // null means any
    val food: FoodPreference? = null // null means any
)

enum class GenderPreference {
    ANY, MALE, FEMALE, OTHER
}

enum class ListingStatus {
    ACTIVE, PAUSED, FILLED
}
