package com.nestmate.app.feature.requirement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Requirement
import com.nestmate.app.data.model.RoomType
import com.nestmate.app.data.repository.RequirementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RequirementFeedViewModel(
    private val requirementRepository: RequirementRepository
) : ViewModel() {

    data class FilterState(
        val city: String = "",
        val roomType: RoomType? = null,
        val minRent: String = ""
    )

    data class UiState(
        val isLoading: Boolean = true,
        val requirements: List<Requirement> = emptyList(),
        val filters: FilterState = FilterState(),
        val isFilterSheetVisible: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val filterStateFlow = MutableStateFlow(FilterState())

    init {
        viewModelScope.launch {
            combine(
                requirementRepository.getActiveRequirementsStream(),
                filterStateFlow
            ) { result, filters ->
                when (result) {
                    is DataResult.Success -> {
                        val filtered = result.data.filter { req ->
                            val matchCity = filters.city.isBlank() || req.preferredLocations.any { it.equals(filters.city.trim(), ignoreCase = true) }
                            val matchRoomType = filters.roomType == null || req.roomType == filters.roomType
                            val matchRent = filters.minRent.toDoubleOrNull()?.let { min -> req.budgetMax >= min } ?: true
                            matchCity && matchRoomType && matchRent
                        }
                        _uiState.update { it.copy(isLoading = false, requirements = filtered, filters = filters, errorMessage = null) }
                    }
                    is DataResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }.collect {}
        }
    }

    fun showFilterSheet() = _uiState.update { it.copy(isFilterSheetVisible = true) }
    fun hideFilterSheet() = _uiState.update { it.copy(isFilterSheetVisible = false) }

    fun applyFilters(city: String, roomType: RoomType?, minRent: String) {
        filterStateFlow.value = FilterState(city, roomType, minRent)
        hideFilterSheet()
    }

    fun clearFilters() {
        filterStateFlow.value = FilterState()
        hideFilterSheet()
    }

    companion object {
        fun provideFactory(
            requirementRepository: RequirementRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RequirementFeedViewModel(requirementRepository) as T
        }
    }
}
