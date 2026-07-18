package com.nestmate.app.feature.requirement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestmate.app.data.model.Requirement
import com.nestmate.app.data.model.RoomType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RequirementFeedScreen(
    viewModel: RequirementFeedViewModel,
    onRequirementClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isFilterSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = viewModel::hideFilterSheet
        ) {
            FilterSheetContent(
                initialFilters = state.filters,
                onApply = viewModel::applyFilters,
                onClear = viewModel::clearFilters
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalIconButton(onClick = viewModel::showFilterSheet) {
                Icon(Icons.Default.Search, contentDescription = "Filter")
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (state.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                SelectionContainer {
                    Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            return
        }

        if (state.requirements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No seekers match your filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.requirements, key = { it.id }) { req ->
                RequirementCard(
                    requirement = req,
                    onClick = { onRequirementClick(req.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterSheetContent(
    initialFilters: RequirementFeedViewModel.FilterState,
    onApply: (city: String, roomType: RoomType?, minRent: String) -> Unit,
    onClear: () -> Unit
) {
    var city by remember { mutableStateOf(initialFilters.city) }
    var roomType by remember { mutableStateOf(initialFilters.roomType) }
    var minRent by remember { mutableStateOf(initialFilters.minRent) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp), // Extra padding for safe area
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Filter Seekers", style = MaterialTheme.typography.titleLarge)
        
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = minRent,
            onValueChange = { minRent = it },
            label = { Text("My Room's Rent (Min Budget)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Text("Room Type", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoomType.entries.forEach { type ->
                FilterChip(
                    selected = roomType == type,
                    onClick = { roomType = if (roomType == type) null else type },
                    label = { Text(type.name) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                Text("Clear")
            }
            Button(
                onClick = { onApply(city, roomType, minRent) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
        }
    }
}

@Composable
private fun RequirementCard(
    requirement: Requirement,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = requirement.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Budget: ${requirement.currency} ${requirement.budgetMin} - ${requirement.budgetMax}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (requirement.preferredLocations.isNotEmpty()) {
                Text(
                    text = "Locations: ${requirement.preferredLocations.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
