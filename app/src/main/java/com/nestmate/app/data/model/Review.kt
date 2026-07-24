package com.nestmate.app.data.model

data class Review(
    val id: String = "",
    val targetType: String = "", // "USER" or "LISTING"
    val targetId: String = "",
    val reviewerUid: String = "",
    val rating: Float = 0f,
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
