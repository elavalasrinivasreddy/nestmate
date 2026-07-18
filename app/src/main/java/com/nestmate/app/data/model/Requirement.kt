package com.nestmate.app.data.model

data class Requirement(
    val id: String = "",
    val seekerUid: String = "",
    val title: String = "",
    val description: String = "",
    val budgetMin: Double = 0.0,
    val budgetMax: Double = 0.0,
    val currency: String = "INR",
    val preferredLocations: List<String> = emptyList(),
    val moveInDate: Long = System.currentTimeMillis(),
    val roomType: RoomType = RoomType.PRIVATE,
    val lifestyle: Lifestyle? = null,
    val status: RequirementStatus = RequirementStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RequirementStatus {
    ACTIVE, PAUSED, FULFILLED
}
