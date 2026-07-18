package com.nestmate.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Requirement
import com.nestmate.app.data.model.RequirementStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRequirementRepository(
    private val firestore: FirebaseFirestore
) : RequirementRepository {

    private val requirementsCollection = firestore.collection("requirements")

    override fun getActiveRequirementsStream(): Flow<DataResult<List<Requirement>>> = callbackFlow {
        val subscription = requirementsCollection
            .whereEqualTo("status", RequirementStatus.ACTIVE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Failed to fetch requirements", error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val requirements = snapshot.documents.mapNotNull { it.toObject(Requirement::class.java) }
                        trySend(DataResult.Success(requirements))
                    } catch (e: Exception) {
                        trySend(DataResult.Error("Failed to parse requirements", e))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getRequirementStream(requirementId: String): Flow<DataResult<Requirement?>> = callbackFlow {
        val subscription = requirementsCollection.document(requirementId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(DataResult.Error(error.message ?: "Failed to fetch requirement", error))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                try {
                    trySend(DataResult.Success(snapshot.toObject(Requirement::class.java)))
                } catch (e: Exception) {
                    trySend(DataResult.Error("Failed to parse requirement", e))
                }
            } else {
                trySend(DataResult.Success(null))
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getRequirementsByOwnerStream(uid: String): Flow<DataResult<List<Requirement>>> = callbackFlow {
        val subscription = requirementsCollection
            .whereEqualTo("seekerUid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Failed to fetch your requirements", error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val requirements = snapshot.documents.mapNotNull { it.toObject(Requirement::class.java) }
                        trySend(DataResult.Success(requirements))
                    } catch (e: Exception) {
                        trySend(DataResult.Error("Failed to parse your requirements", e))
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveRequirement(requirement: Requirement): DataResult<String> {
        return try {
            val isNew = requirement.id.isEmpty()
            val docRef = if (isNew) requirementsCollection.document() else requirementsCollection.document(requirement.id)
            
            val requirementToSave = requirement.copy(
                id = docRef.id,
                updatedAt = System.currentTimeMillis(),
                createdAt = if (isNew) System.currentTimeMillis() else requirement.createdAt
            )
            
            docRef.set(requirementToSave).await()
            DataResult.Success(docRef.id)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to save requirement", e)
        }
    }

    override suspend fun deleteRequirement(requirementId: String): DataResult<Unit> {
        return try {
            requirementsCollection.document(requirementId).delete().await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Failed to delete requirement", e)
        }
    }
}
