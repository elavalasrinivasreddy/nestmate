package com.nestmate.app.data.repository

import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.ContextType
import com.nestmate.app.data.model.Report

interface TrustRepository {
    /** Files a report against a user, listing, or requirement. */
    suspend fun reportUser(
        reportedUid: String,
        reason: String,
        contextType: ContextType,
        contextId: String?
    ): DataResult<Unit>

    /** Blocks a user so their content is hidden and they can't message. */
    suspend fun blockUser(uidToBlock: String): DataResult<Unit>
}
