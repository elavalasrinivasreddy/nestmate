package com.nestmate.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nestmate.app.NestmateApplication
import com.nestmate.app.feature.auth.AuthScreen
import com.nestmate.app.feature.chat.MessageThreadScreen
import com.nestmate.app.feature.chat.MessageThreadViewModel
import com.nestmate.app.feature.home.HomeScreen
import com.nestmate.app.feature.listing.CreateEditListingViewModel
import com.nestmate.app.feature.listing.CreateListingScreen
import com.nestmate.app.feature.listing.ListingDetailScreen
import com.nestmate.app.feature.listing.ListingDetailViewModel
import com.nestmate.app.feature.profile.ProfileScreen
import com.nestmate.app.feature.requirement.CreateEditRequirementViewModel
import com.nestmate.app.feature.requirement.CreateRequirementScreen
import com.nestmate.app.feature.requirement.RequirementDetailScreen
import com.nestmate.app.feature.requirement.RequirementDetailViewModel
import com.nestmate.app.feature.welcome.WelcomeScreen

/**
 * Single source of navigation. Start destination is gated on auth state:
 * signed-in users go straight to Home, everyone else starts at Welcome.
 * The graph grows phase by phase.
 */
@Composable
fun NestmateNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as NestmateApplication).container }

    val startDestination = if (container.authRepository.currentUser != null) {
        Destination.Home.route
    } else {
        Destination.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Destination.Welcome.route) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Destination.Auth.route) }
            )
        }
        composable(Destination.Auth.route) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Destination.Home.route) {
            HomeScreen(
                onSignOut = {
                    navController.navigate(Destination.Welcome.route) {
                        popUpTo(Destination.Home.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = { navController.navigate(Destination.Profile.route) },
                onNavigateToCreateListing = { navController.navigate(Destination.CreateListing().route) },
                onNavigateToListingDetail = { id -> navController.navigate(Destination.ListingDetail(id).route) },
                onNavigateToCreateRequirement = { navController.navigate(Destination.CreateRequirement().route) },
                onNavigateToRequirementDetail = { id -> navController.navigate(Destination.RequirementDetail(id).route) },
                onNavigateToThread = { id -> navController.navigate(Destination.MessageThread(id).route) }
            )
        }
        composable(Destination.Profile.route) {
            ProfileScreen(
                onProfileSaved = {
                    navController.popBackStack()
                }
            )
        }
        composable(Destination.CreateListing.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("id")
            val createListingViewModel: CreateEditListingViewModel = viewModel(
                factory = CreateEditListingViewModel.provideFactory(
                    container.authRepository,
                    container.listingRepository,
                    listingId
                )
            )
            CreateListingScreen(
                viewModel = createListingViewModel,
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Destination.ListingDetail.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("id") ?: return@composable
            val detailViewModel: ListingDetailViewModel = viewModel(
                factory = ListingDetailViewModel.provideFactory(
                    container.authRepository,
                    container.listingRepository,
                    container.chatRepository,
                    container.bookmarkRepository,
                    listingId
                )
            )
            ListingDetailScreen(
                viewModel = detailViewModel,
                onEdit = { navController.navigate(Destination.CreateListing(listingId).route) },
                onDeleted = { navController.popBackStack() },
                onMessageClick = { conversationId ->
                    navController.navigate(Destination.MessageThread(conversationId).route)
                }
            )
        }
        composable(Destination.CreateRequirement.route) { backStackEntry ->
            val reqId = backStackEntry.arguments?.getString("id")
            val createReqViewModel: CreateEditRequirementViewModel = viewModel(
                factory = CreateEditRequirementViewModel.provideFactory(
                    container.authRepository,
                    container.requirementRepository,
                    reqId
                )
            )
            CreateRequirementScreen(
                viewModel = createReqViewModel,
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Destination.RequirementDetail.route) { backStackEntry ->
            val reqId = backStackEntry.arguments?.getString("id") ?: return@composable
            val reqDetailViewModel: RequirementDetailViewModel = viewModel(
                factory = RequirementDetailViewModel.provideFactory(
                    container.authRepository,
                    container.requirementRepository,
                    container.chatRepository,
                    container.bookmarkRepository,
                    reqId
                )
            )
            RequirementDetailScreen(
                viewModel = reqDetailViewModel,
                onEdit = { navController.navigate(Destination.CreateRequirement(reqId).route) },
                onDeleted = { navController.popBackStack() },
                onMessageClick = { conversationId ->
                    navController.navigate(Destination.MessageThread(conversationId).route)
                }
            )
        }
        composable(Destination.MessageThread.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            val threadViewModel: MessageThreadViewModel = viewModel(
                factory = MessageThreadViewModel.provideFactory(
                    container.authRepository,
                    container.chatRepository,
                    conversationId
                )
            )
            MessageThreadScreen(
                viewModel = threadViewModel
            )
        }
    }
}
