package com.nestmate.app.data.model

data class Conversation(
    val id: String = "",
    val participantUids: List<String> = emptyList(),
    val participantsMeta: Map<String, ParticipantMeta> = emptyMap(),
    val context: ConversationContext? = null,
    val lastMessage: LastMessage? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ParticipantMeta(
    val displayName: String = "",
    val photoUrl: String? = null
)

data class ConversationContext(
    val type: ContextType = ContextType.LISTING,
    val id: String = ""
)

enum class ContextType {
    LISTING, REQUIREMENT
}

data class LastMessage(
    val text: String = "",
    val senderUid: String = "",
    val sentAt: Long = System.currentTimeMillis()
)
