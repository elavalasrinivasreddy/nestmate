package com.nestmate.app.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nestmate.app.NestmateApplication
import com.nestmate.app.feature.listing.ListingFeedScreen
import com.nestmate.app.feature.listing.ListingFeedViewModel

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onNavigateToListingDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(
            (LocalContext.current.applicationContext as NestmateApplication).container.authRepository,
            (LocalContext.current.applicationContext as NestmateApplication).container.profileRepository
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        if (!state.hasProfile) {
            // Onboarding State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to Nestmate!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "To start finding rooms or roommates, we need to know a little bit about you.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Create Profile")
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Sign out")
                }
            }
        } else {
            // Dashboard State - Listing Feed
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(onClick = onNavigateToCreateListing) {
                        Icon(Icons.Default.Add, contentDescription = "Post a Room")
                    }
                }
            ) { innerPadding ->
                val feedViewModel: ListingFeedViewModel = viewModel(
                    factory = ListingFeedViewModel.provideFactory(
                        (LocalContext.current.applicationContext as NestmateApplication).container.listingRepository
                    )
                )
                
                Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hi, ${state.profile?.displayName ?: "there"}!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            TextButton(onClick = onNavigateToProfile) {
                                Text("Profile")
                            }
                            TextButton(onClick = {
                                viewModel.signOut()
                                onSignOut()
                            }) {
                                Text("Sign out")
                            }
                        }
                    }
                    
                    ListingFeedScreen(
                        viewModel = feedViewModel,
                        onListingClick = onNavigateToListingDetail,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
