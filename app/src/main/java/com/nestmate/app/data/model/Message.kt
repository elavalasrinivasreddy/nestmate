package com.nestmate.app.data.model

data class Message(
    val id: String = "",
    val senderUid: String = "",
    val text: String = "",
    val sentAt: Long = System.currentTimeMillis(),
    val readBy: List<String>? = null
)
