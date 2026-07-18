package com.nestmate.app.data.repository

import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Requirement
import kotlinx.coroutines.flow.Flow

interface RequirementRepository {
    /** Gets a stream of active requirements, ordered by newest first. */
    fun getActiveRequirementsStream(): Flow<DataResult<List<Requirement>>>

    /** Gets a stream of a single requirement by ID. */
    fun getRequirementStream(requirementId: String): Flow<DataResult<Requirement?>>

    /** Gets a stream of requirements owned by a specific user. */
    fun getRequirementsByOwnerStream(uid: String): Flow<DataResult<List<Requirement>>>

    /** Saves (creates or updates) a requirement. */
    suspend fun saveRequirement(requirement: Requirement): DataResult<String>

    /** Deletes a requirement. */
    suspend fun deleteRequirement(requirementId: String): DataResult<Unit>
}
