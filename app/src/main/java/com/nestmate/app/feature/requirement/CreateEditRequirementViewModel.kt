package com.nestmate.app.feature.requirement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nestmate.app.core.common.DataResult
import com.nestmate.app.data.model.Requirement
import com.nestmate.app.data.model.RoomType
import com.nestmate.app.data.repository.AuthRepository
import com.nestmate.app.data.repository.RequirementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateEditRequirementViewModel(
    private val authRepository: AuthRepository,
    private val requirementRepository: RequirementRepository,
    private val requirementId: String? = null
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val isSaved: Boolean = false,
        val requirement: Requirement = Requirement()
    ) {
        val canSave: Boolean
            get() = requirement.title.isNotBlank() &&
                    requirement.description.isNotBlank() &&
                    requirement.budgetMin > 0 &&
                    requirement.budgetMax >= requirement.budgetMin &&
                    requirement.preferredLocations.isNotEmpty() &&
                    !isSaving
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        if (requirementId != null) {
            loadRequirement(requirementId)
        } else {
            val ownerUid = authRepository.currentUser?.uid ?: ""
            _uiState.update { it.copy(isLoading = false, requirement = Requirement(seekerUid = ownerUid)) }
        }
    }

    private fun loadRequirement(id: String) {
        viewModelScope.launch {
            val result = requirementRepository.getRequirementStream(id).first()
            when (result) {
                is DataResult.Success -> {
                    if (result.data != null) {
                        _uiState.update { it.copy(isLoading = false, requirement = result.data) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Requirement not found") }
                    }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onTitleChange(value: String) = updateRequirement { it.copy(title = value) }
    fun onDescriptionChange(value: String) = updateRequirement { it.copy(description = value) }
    fun onBudgetMinChange(value: String) {
        val min = value.toDoubleOrNull() ?: return
        updateRequirement { it.copy(budgetMin = min) }
    }
    fun onBudgetMaxChange(value: String) {
        val max = value.toDoubleOrNull() ?: return
        updateRequirement { it.copy(budgetMax = max) }
    }
    fun addLocation(location: String) {
        if (location.isNotBlank()) {
            updateRequirement { 
                val newList = it.preferredLocations.toMutableList().apply { 
                    if (!contains(location)) add(location) 
                }
                it.copy(preferredLocations = newList) 
            }
        }
    }
    fun removeLocation(location: String) {
        updateRequirement { it.copy(preferredLocations = it.preferredLocations - location) }
    }
    fun onRoomTypeChange(type: RoomType) = updateRequirement { it.copy(roomType = type) }

    private fun updateRequirement(transform: (Requirement) -> Requirement) {
        _uiState.update { it.copy(requirement = transform(it.requirement), errorMessage = null) }
    }

    fun saveRequirement() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = requirementRepository.saveRequirement(state.requirement)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            requirementRepository: RequirementRepository,
            requirementId: String? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CreateEditRequirementViewModel(authRepository, requirementRepository, requirementId) as T
        }
    }
}
