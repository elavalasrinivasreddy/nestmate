package com.nestmate.app.feature.listing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestmate.app.data.model.Listing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListingDetailScreen(
    viewModel: ListingDetailViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val listing = state.listing
    if (listing == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Listing not found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = listing.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${listing.currency} ${listing.rentAmount}/month",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Details", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(onClick = { }, label = { Text(listing.roomType.name) })
            SuggestionChip(onClick = { }, label = { Text("${listing.location.area}, ${listing.location.city}") })
            SuggestionChip(onClick = { }, label = { Text(listing.status.name) })
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = listing.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Roommate Preferences", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(onClick = { }, label = { Text("Gender: ${listing.preferences.gender.name}") })
            listing.preferences.occupationType?.let {
                SuggestionChip(onClick = { }, label = { Text("Occupation: ${it.name}") })
            }
            listing.preferences.smoking?.let {
                SuggestionChip(onClick = { }, label = { Text("Smoking: ${it.name}") })
            }
            listing.preferences.food?.let {
                SuggestionChip(onClick = { }, label = { Text("Food: ${it.name}") })
            }
        }
    }
}
