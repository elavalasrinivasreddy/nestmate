package com.nestmate.app.feature.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Bookmark
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookmarkListViewModel(
    private val authRepository: AuthRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val bookmarks: List<Bookmark> = emptyList(),
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            bookmarkRepository.getBookmarksStream(uid).collectLatest { result ->
                when (result) {
                    is DataResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, bookmarks = result.data) }
                    }
                    is DataResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            bookmarkRepository: BookmarkRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BookmarkListViewModel(authRepository, bookmarkRepository) as T
        }
    }
}
