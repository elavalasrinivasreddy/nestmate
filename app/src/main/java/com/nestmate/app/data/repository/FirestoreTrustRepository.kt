package com.nestmate.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.ContextType
import com.nestmate.app.data.model.Report
import kotlinx.coroutines.tasks.await

class FirestoreTrustRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : TrustRepository {

    override suspend fun reportUser(
        reportedUid: String,
        reason: String,
        contextType: ContextType,
        contextId: String?
    ): DataResult<Unit> {
        val currentUid = authRepository.currentUser?.uid ?: return DataResult.Error("Not signed in")
        
        return try {
            val reportRef = firestore.collection("reports").document()
            val report = Report(
                id = reportRef.id,
                reporterUid = currentUid,
                reportedUid = reportedUid,
                reason = reason,
                contextType = contextType,
                contextId = contextId
            )
            reportRef.set(report).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to submit report", e)
        }
    }

    override suspend fun blockUser(uidToBlock: String): DataResult<Unit> {
        val currentUid = authRepository.currentUser?.uid ?: return DataResult.Error("Not signed in")
        if (currentUid == uidToBlock) return DataResult.Error("Cannot block yourself")
        
        return try {
            firestore.collection("users").document(currentUid)
                .update("blockedUids", FieldValue.arrayUnion(uidToBlock))
                .await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to block user", e)
        }
    }
}
