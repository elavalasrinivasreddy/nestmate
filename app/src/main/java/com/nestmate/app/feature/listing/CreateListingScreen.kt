package com.nestmate.app.feature.listing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestmate.app.core.common.Locations
import com.nestmate.app.core.designsystem.AreaPickerField
import com.nestmate.app.data.model.GenderPreference
import com.nestmate.app.data.model.RoomType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateListingScreen(
    viewModel: CreateEditListingViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = viewModel::saveListing,
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Post Listing")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Post a Room", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }

            if (state.errorMessage != null) {
                item {
                    Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                OutlinedTextField(
                    value = state.listing.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
            item {
                OutlinedTextField(
                    value = state.listing.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = if (state.listing.rentAmount == 0.0) "" else state.listing.rentAmount.toString(),
                        onValueChange = viewModel::onRentAmountChange,
                        label = { Text("Rent/month *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = state.listing.location.city,
                        onValueChange = viewModel::onCityChange,
                        label = { Text("City *") },
                        modifier = Modifier.weight(1f)
                    )
                    AreaPickerField(
                        value = state.listing.location.area,
                        onValueChange = { area ->
                            viewModel.onAreaChange(area)
                            Locations.cityForArea(area)?.let { viewModel.onCityChange(it) }
                        },
                        label = "Area *",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Text("Room Type", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoomType.entries.forEach { type ->
                        FilterChip(
                            selected = state.listing.roomType == type,
                            onClick = { viewModel.onRoomTypeChange(type) },
                            label = { Text(type.name) }
                        )
                    }
                }
            }
            item {
                Text("Preferred Gender", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GenderPreference.entries.forEach { type ->
                        FilterChip(
                            selected = state.listing.preferences.gender == type,
                            onClick = { viewModel.onGenderPreferenceChange(type) },
                            label = { Text(type.name) }
                        )
                    }
                }
            }
        }
    }
}
