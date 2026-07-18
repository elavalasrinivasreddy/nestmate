package com.nestmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.ContextType
import com.nestmate.app.data.model.Conversation
import com.nestmate.app.data.model.ConversationContext
import com.nestmate.app.data.model.LastMessage
import com.nestmate.app.data.model.Message
import com.nestmate.app.data.model.ParticipantMeta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ChatRepository {

    private val conversationsCollection = firestore.collection("conversations")

    override suspend fun createOrGetConversation(
        otherUserId: String,
        contextType: ContextType,
        contextId: String
    ): DataResult<String> {
        val currentUid = authRepository.currentUser?.uid ?: return DataResult.Error("Not signed in")
        if (currentUid == otherUserId) return DataResult.Error("Cannot chat with yourself")

        // Create a consistent ID based on participants and context so we don't duplicate
        val participants = listOf(currentUid, otherUserId).sorted()
        val conversationId = "${participants[0]}_${participants[1]}_${contextType.name}_$contextId"

        return try {
            val docRef = conversationsCollection.document(conversationId)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                DataResult.Success(conversationId)
            } else {
                // Fetch profiles for metadata
                val currentUserProfileResult = profileRepository.getProfile(currentUid)
                val otherUserProfileResult = profileRepository.getProfile(otherUserId)

                val currentName = (currentUserProfileResult as? DataResult.Success)?.data?.displayName ?: "User"
                val otherName = (otherUserProfileResult as? DataResult.Success)?.data?.displayName ?: "User"

                val newConversation = Conversation(
                    id = conversationId,
                    participantUids = participants,
                    participantsMeta = mapOf(
                        currentUid to ParticipantMeta(displayName = currentName),
                        otherUserId to ParticipantMeta(displayName = otherName)
                    ),
                    context = ConversationContext(type = contextType, id = contextId),
                    updatedAt = System.currentTimeMillis()
                )

                docRef.set(newConversation).await()
                DataResult.Success(conversationId)
            }
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to initialize conversation", e)
        }
    }

    override fun getConversationsStream(): Flow<DataResult<List<Conversation>>> = callbackFlow {
        val currentUid = authRepository.currentUser?.uid
        if (currentUid == null) {
            trySend(DataResult.Error("Not signed in"))
            close()
            return@callbackFlow
        }

        val subscription = conversationsCollection
            .whereArrayContains("participantUids", currentUid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Failed to fetch conversations", error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val convos = snapshot.documents.mapNotNull { it.toObject(Conversation::class.java) }
                        trySend(DataResult.Success(convos))
                    } catch (e: Exception) {
                        trySend(DataResult.Error("Failed to parse conversations", e))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getMessagesStream(conversationId: String): Flow<DataResult<List<Message>>> = callbackFlow {
        val subscription = conversationsCollection.document(conversationId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Failed to fetch messages", error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val msgs = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                        trySend(DataResult.Success(msgs))
                    } catch (e: Exception) {
                        trySend(DataResult.Error("Failed to parse messages", e))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getConversationStream(conversationId: String): Flow<DataResult<Conversation?>> = callbackFlow {
        val subscription = conversationsCollection.document(conversationId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(DataResult.Error(error.message ?: "Failed to fetch conversation", error))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(DataResult.Success(snapshot.toObject(Conversation::class.java)))
            } else {
                trySend(DataResult.Success(null))
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendMessage(conversationId: String, text: String): DataResult<Unit> {
        val currentUid = authRepository.currentUser?.uid ?: return DataResult.Error("Not signed in")
        
        return try {
            val batch = firestore.batch()
            val convoRef = conversationsCollection.document(conversationId)
            val newMsgRef = convoRef.collection("messages").document()
            
            val now = System.currentTimeMillis()
            val message = Message(
                id = newMsgRef.id,
                senderUid = currentUid,
                text = text,
                sentAt = now
            )
            
            // Add message
            batch.set(newMsgRef, message)
            
            // Update conversation lastMessage & updatedAt
            val lastMsg = LastMessage(text = text, senderUid = currentUid, sentAt = now)
            batch.update(convoRef, mapOf(
                "lastMessage" to lastMsg,
                "updatedAt" to now
            ))
            
            batch.commit().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to send message", e)
        }
    }
}
