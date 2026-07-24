package com.nestmate.app.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
            (LocalContext.current.applicationContext as NestmateApplication).container.profileRepository,
            (LocalContext.current.applicationContext as NestmateApplication).container.reviewRepository
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

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.actionSuccessMessage) {
        state.actionSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var newLocation by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        // Auto-commit any pending text in the location field so it isn't lost on save.
                        if (newLocation.isNotBlank()) {
                            viewModel.addLocation(newLocation.trim())
                            newLocation = ""
                        }
                        viewModel.saveProfile()
                    },
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero header: identity + trust signals.
            item { ProfileHeader(state.profile) }

            if (state.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item {
                ProfileSection(
                    title = "About You",
                    subtitle = "How other members will see you."
                ) {
                    OutlinedTextField(
                        value = state.profile.displayName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Full name") },
                        isError = state.profile.displayName.isBlank(),
                        supportingText = {
                            if (state.profile.displayName.isBlank()) Text("Required")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

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

                    ChipGroup(label = "I am a") {
                        UserType.entries.forEach { type ->
                            FilterChip(
                                selected = state.profile.userType == type,
                                onClick = { viewModel.onUserTypeChange(type) },
                                label = { Text(type.name.replace("_", " ")) }
                            )
                        }
                    }

                    ChipGroup(label = "Occupation") {
                        OccupationType.entries.forEach { type ->
                            FilterChip(
                                selected = state.profile.occupationType == type,
                                onClick = { viewModel.onOccupationChange(type) },
                                label = { Text(type.name.replace("_", " ")) }
                            )
                        }
                    }
                }
            }

            item {
                ProfileSection(
                    title = "Lifestyle",
                    subtitle = "Helps us match you with compatible roommates."
                ) {
                    LifestyleSelector(
                        title = "Smoking",
                        options = SmokingPreference.entries,
                        selected = state.profile.lifestyle.smoking,
                        onSelect = viewModel::onSmokingChange
                    )
                    LifestyleSelector(
                        title = "Drinking",
                        options = DrinkingPreference.entries,
                        selected = state.profile.lifestyle.drinking,
                        onSelect = viewModel::onDrinkingChange
                    )
                    LifestyleSelector(
                        title = "Food",
                        options = FoodPreference.entries,
                        selected = state.profile.lifestyle.food,
                        onSelect = viewModel::onFoodChange
                    )
                    LifestyleSelector(
                        title = "Sleep schedule",
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

            item {
                ProfileSection(
                    title = "Preferred Locations",
                    subtitle = "Cities or areas where you'd like to live."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newLocation,
                            onValueChange = { newLocation = it },
                            label = { Text("Add city / area") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        FilledIconButton(
                            onClick = {
                                if (newLocation.isNotBlank()) {
                                    viewModel.addLocation(newLocation.trim())
                                    newLocation = ""
                                }
                            },
                            enabled = newLocation.isNotBlank(),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add location")
                        }
                    }

                    if (state.profile.preferredLocations.isEmpty()) {
                        Text(
                            text = "No locations added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.profile.preferredLocations.forEach { loc ->
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.removeLocation(loc) },
                                    label = { Text(loc) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove $loc",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                ProfileSection(title = "Reviews") {
                    when {
                        state.isReviewsLoading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        state.reviewsError != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Failed to load reviews.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        state.reviews.isEmpty() -> Text(
                            "No reviews yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.reviews.take(5).forEachIndexed { index, review ->
                                if (index > 0) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                                ReviewRow(review)
                            }
                        }
                    }

                    var showReviewDialog by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { showReviewDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leave a Review")
                    }

                    if (showReviewDialog) {
                        LeaveReviewDialog(
                            onDismiss = { showReviewDialog = false },
                            onSubmit = { rating, text ->
                                viewModel.submitReview(rating, text)
                                showReviewDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile) {
    Column(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f (%d reviews)", profile.averageRating, profile.reviewCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (profile.verification.phoneVerified) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Phone verified (${profile.phoneNumber})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Grouped form section: an ElevatedCard with a titled header and evenly spaced content. */
@Composable
private fun ProfileSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

/** A labelled group of chips (label uses labelLarge weight). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(label: String, content: @Composable FlowRowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun ReviewRow(review: Review) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = review.rating.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = review.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Anonymous",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaveReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(5f) }
    var reviewText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave a Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Rating: ${rating.toInt()} / 5",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, reviewText) },
                enabled = reviewText.isNotBlank()
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T : Enum<T>> LifestyleSelector(
    title: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    ChipGroup(label = title) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.name.replace("_", " ")) }
            )
        }
    }
}
