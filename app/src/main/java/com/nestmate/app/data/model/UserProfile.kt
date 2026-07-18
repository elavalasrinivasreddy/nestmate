package com.nestmate.app.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val phoneNumber: String? = null,
    val userType: UserType = UserType.SEEKER,
    val occupationType: OccupationType = OccupationType.PROFESSIONAL,
    val bio: String = "",
    val preferredLocations: List<String> = emptyList(),
    val lifestyle: Lifestyle = Lifestyle(),
    val verification: Verification = Verification(),
    val blockedUids: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserType {
    SEEKER, ROOM_HOLDER, BOTH
}

enum class OccupationType {
    STUDENT, PROFESSIONAL, OTHER
}

data class Lifestyle(
    val smoking: SmokingPreference = SmokingPreference.NO,
    val food: FoodPreference = FoodPreference.ANYTHING,
    val sleepSchedule: SleepSchedule = SleepSchedule.FLEXIBLE,
    val cleanliness: Cleanliness = Cleanliness.MODERATE
)

enum class SmokingPreference {
    YES, NO, OUTSIDE
}

enum class FoodPreference {
    VEG, NON_VEG, VEGAN, ANYTHING
}

enum class SleepSchedule {
    EARLY_BIRD, NIGHT_OWL, FLEXIBLE
}

enum class Cleanliness {
    NEAT_FREAK, MODERATE, MESSY
}

data class Verification(
    val phoneVerified: Boolean = false
)
