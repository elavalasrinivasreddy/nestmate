package com.nestmate.app.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nestmate.app.NestmateApplication
import com.nestmate.app.data.model.UserType
import com.nestmate.app.feature.bookmark.BookmarkListScreen
import com.nestmate.app.feature.bookmark.BookmarkListViewModel
import com.nestmate.app.feature.chat.ConversationListScreen
import com.nestmate.app.feature.chat.ConversationListViewModel
import com.nestmate.app.feature.listing.ListingFeedScreen
import com.nestmate.app.feature.listing.ListingFeedViewModel
import com.nestmate.app.feature.requirement.RequirementFeedScreen
import com.nestmate.app.feature.requirement.RequirementFeedViewModel

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onNavigateToListingDetail: (String) -> Unit,
    onNavigateToCreateRequirement: () -> Unit,
    onNavigateToRequirementDetail: (String) -> Unit,
    onNavigateToThread: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(
            (LocalContext.current.applicationContext as NestmateApplication).container.authRepository,
            (LocalContext.current.applicationContext as NestmateApplication).container.profileRepository
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val container = remember { (context.applicationContext as NestmateApplication).container }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to Nestmate!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
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
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Create Profile", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Sign out")
                }
            }
        } else {
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Rooms", "Seekers", "Saved", "Inbox", "Profile")
            val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Favorite, Icons.Default.Email, Icons.Default.Person)

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            NavigationBarItem(
                                icon = { Icon(icons[index], contentDescription = title) },
                                label = { Text(title) },
                                selected = selectedTab == index,
                                onClick = { selectedTab = index }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    val userType = state.profile?.userType
                    
                    // Logic fix: Only show FABs appropriate to the user's type
                    val showPostRoom = (userType == UserType.ROOM_HOLDER || userType == UserType.BOTH) && selectedTab == 0
                    val showPostRequirement = (userType == UserType.SEEKER || userType == UserType.BOTH) && selectedTab == 1

                    if (showPostRoom) {
                        ExtendedFloatingActionButton(
                            onClick = onNavigateToCreateListing,
                            icon = { Icon(Icons.Default.Add, contentDescription = "Post a Room") },
                            text = { Text("Post Room") }
                        )
                    } else if (showPostRequirement) {
                        ExtendedFloatingActionButton(
                            onClick = onNavigateToCreateRequirement,
                            icon = { Icon(Icons.Default.Add, contentDescription = "Post a Requirement") },
                            text = { Text("Seek Room") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when (selectedTab) {
                        0 -> {
                            val feedViewModel: ListingFeedViewModel = viewModel(
                                factory = ListingFeedViewModel.provideFactory(
                                    container.authRepository,
                                    container.profileRepository,
                                    container.listingRepository
                                )
                            )
                            ListingFeedScreen(viewModel = feedViewModel, onListingClick = onNavigateToListingDetail)
                        }
                        1 -> {
                            val reqViewModel: RequirementFeedViewModel = viewModel(
                                factory = RequirementFeedViewModel.provideFactory(
                                    container.authRepository,
                                    container.profileRepository,
                                    container.requirementRepository
                                )
                            )
                            RequirementFeedScreen(viewModel = reqViewModel, onRequirementClick = onNavigateToRequirementDetail)
                        }
                        2 -> {
                            val bookmarkViewModel: BookmarkListViewModel = viewModel(
                                factory = BookmarkListViewModel.provideFactory(container.authRepository, container.bookmarkRepository)
                            )
                            BookmarkListScreen(
                                viewModel = bookmarkViewModel,
                                onListingClick = onNavigateToListingDetail,
                                onRequirementClick = onNavigateToRequirementDetail
                            )
                        }
                        3 -> {
                            val inboxViewModel: ConversationListViewModel = viewModel(
                                factory = ConversationListViewModel.provideFactory(
                                    container.authRepository,
                                    container.profileRepository,
                                    container.chatRepository
                                )
                            )
                            ConversationListScreen(
                                viewModel = inboxViewModel,
                                authRepository = container.authRepository,
                                onConversationClick = onNavigateToThread
                            )
                        }
                        4 -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                                Spacer(Modifier.height(24.dp))
                                Text("Hi, ${state.profile?.displayName ?: "there"}!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(state.profile?.phoneNumber ?: "", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(32.dp))
                                Button(onClick = onNavigateToProfile, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Edit Profile") }
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(onClick = { viewModel.signOut(); onSignOut() }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Sign out") }
                            }
                        }
                    }
                }
            }
        }
    }
}
