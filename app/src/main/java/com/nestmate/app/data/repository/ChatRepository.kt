package com.nestmate.app.data.repository

import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.ContextType
import com.nestmate.app.data.model.Conversation
import com.nestmate.app.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /** 
     * Creates a new conversation or gets an existing one between currentUser and otherUserId 
     * for a specific context (Listing or Requirement).
     */
    suspend fun createOrGetConversation(
        otherUserId: String,
        contextType: ContextType,
        contextId: String
    ): DataResult<String>

    /** Gets a stream of all conversations for the current user, ordered by updatedAt. */
    fun getConversationsStream(): Flow<DataResult<List<Conversation>>>

    /** Gets a stream of messages for a specific conversation, ordered by sentAt ASC. */
    fun getMessagesStream(conversationId: String): Flow<DataResult<List<Message>>>

    /** Gets a stream for a single conversation to watch for typing/metadata updates. */
    fun getConversationStream(conversationId: String): Flow<DataResult<Conversation?>>

    /** Sends a message to a conversation. */
    suspend fun sendMessage(conversationId: String, text: String): DataResult<Unit>
}
