package com.nestmate.app.core.common

/**
 * Result wrapper for repository operations, so exceptions never leak into the
 * UI layer (see docs/ARCHITECTURE.md → Error handling).
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : DataResult<Nothing>
}

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onError(action: (message: String, cause: Throwable?) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(message, cause)
    return this
}
