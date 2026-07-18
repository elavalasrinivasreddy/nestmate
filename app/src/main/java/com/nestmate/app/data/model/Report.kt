package com.nestmate.app.data.model

data class Report(
    val id: String = "",
    val reporterUid: String = "",
    val reportedUid: String = "",
    val reason: String = "",
    val contextType: ContextType = ContextType.LISTING,
    val contextId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
