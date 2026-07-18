package com.nestmate.app.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nestmate.app.NestmateApplication
import com.nestmate.app.data.model.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onProfileSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(
            (LocalContext.current.applicationContext as NestmateApplication).container.authRepository,
            (LocalContext.current.applicationContext as NestmateApplication).container.profileRepository
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onProfileSaved()
            viewModel.resetSavedState()
        }
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
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                PaddingValues(16.dp)
                Button(
                    onClick = viewModel::saveProfile,
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Profile")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Your Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (state.profile.verification.phoneVerified) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Phone Verified (${state.profile.phoneNumber})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (state.errorMessage != null) {
                item {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = state.profile.displayName,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }

            item {
                OutlinedTextField(
                    value = state.profile.bio,
                    onValueChange = viewModel::onBioChange,
                    label = { Text("Bio") },
                    placeholder = { Text("Tell us a bit about yourself...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            item {
                SectionTitle("I am a...")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserType.entries.forEach { type ->
                        FilterChip(
                            selected = state.profile.userType == type,
                            onClick = { viewModel.onUserTypeChange(type) },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }
            }

            item {
                SectionTitle("Occupation")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OccupationType.entries.forEach { type ->
                        FilterChip(
                            selected = state.profile.occupationType == type,
                            onClick = { viewModel.onOccupationChange(type) },
                            label = { Text(type.name) }
                        )
                    }
                }
            }

            item {
                SectionTitle("Preferred Locations")
                var newLocation by remember { mutableStateOf("") }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                
                if (state.profile.preferredLocations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.profile.preferredLocations.forEach { loc ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.removeLocation(loc) },
                                label = { Text(loc) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle("Lifestyle")
                
                LifestyleSelector(
                    title = "Smoking",
                    options = SmokingPreference.entries,
                    selected = state.profile.lifestyle.smoking,
                    onSelect = viewModel::onSmokingChange
                )
                
                LifestyleSelector(
                    title = "Food",
                    options = FoodPreference.entries,
                    selected = state.profile.lifestyle.food,
                    onSelect = viewModel::onFoodChange
                )

                LifestyleSelector(
                    title = "Sleep Schedule",
                    options = SleepSchedule.entries,
                    selected = state.profile.lifestyle.sleepSchedule,
                    onSelect = viewModel::onSleepChange
                )

                LifestyleSelector(
                    title = "Cleanliness",
                    options = Cleanliness.entries,
                    selected = state.profile.lifestyle.cleanliness,
                    onSelect = viewModel::onCleanlinessChange
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <T : Enum<T>> LifestyleSelector(
    title: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = { Text(option.name.replace("_", " ")) }
                )
            }
        }
    }
}
