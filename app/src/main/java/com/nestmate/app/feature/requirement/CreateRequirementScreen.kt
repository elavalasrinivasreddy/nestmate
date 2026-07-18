package com.nestmate.app.feature.requirement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestmate.app.data.model.RoomType

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateRequirementScreen(
    viewModel: CreateEditRequirementViewModel,
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
                    onClick = viewModel::saveRequirement,
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Post Requirement")
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
                Text("Post a Requirement", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }

            if (state.errorMessage != null) {
                item {
                    Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                OutlinedTextField(
                    value = state.requirement.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
            item {
                OutlinedTextField(
                    value = state.requirement.description,
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
                        value = if (state.requirement.budgetMin == 0.0) "" else state.requirement.budgetMin.toString(),
                        onValueChange = viewModel::onBudgetMinChange,
                        label = { Text("Min Budget *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = if (state.requirement.budgetMax == 0.0) "" else state.requirement.budgetMax.toString(),
                        onValueChange = viewModel::onBudgetMaxChange,
                        label = { Text("Max Budget *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            
            item {
                Text("Preferred Locations *", style = MaterialTheme.typography.titleMedium)
                var newLocation by remember { mutableStateOf("") }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newLocation,
                        onValueChange = { newLocation = it },
                        label = { Text("Add city/area") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        viewModel.addLocation(newLocation.trim())
                        newLocation = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Location")
                    }
                }
                
                if (state.requirement.preferredLocations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.requirement.preferredLocations.forEach { loc ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.removeLocation(loc) },
                                label = { Text(loc) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            item {
                Text("Room Type", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoomType.entries.forEach { type ->
                        FilterChip(
                            selected = state.requirement.roomType == type,
                            onClick = { viewModel.onRoomTypeChange(type) },
                            label = { Text(type.name) }
                        )
                    }
                }
            }
        }
    }
}
