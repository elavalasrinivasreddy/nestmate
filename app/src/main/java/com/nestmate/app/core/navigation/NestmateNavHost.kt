package com.nestmate.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nestmate.app.NestmateApplication
import com.nestmate.app.feature.auth.AuthScreen
import com.nestmate.app.feature.home.HomeScreen
import com.nestmate.app.feature.profile.ProfileScreen
import com.nestmate.app.feature.listing.CreateEditListingViewModel
import com.nestmate.app.feature.listing.CreateListingScreen
import com.nestmate.app.feature.listing.ListingDetailScreen
import com.nestmate.app.feature.listing.ListingDetailViewModel
import com.nestmate.app.feature.welcome.WelcomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel

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
                onNavigateToCreateListing = { navController.navigate(Destination.CreateListing.route) },
                onNavigateToListingDetail = { id -> navController.navigate(Destination.ListingDetail(id).route) }
            )
        }
        composable(Destination.Profile.route) {
            ProfileScreen(
                onProfileSaved = {
                    navController.popBackStack()
                }
            )
        }
        composable(Destination.CreateListing.route) {
            val createListingViewModel: CreateEditListingViewModel = viewModel(
                factory = CreateEditListingViewModel.provideFactory(
                    container.authRepository,
                    container.listingRepository
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
                    container.listingRepository,
                    listingId
                )
            )
            ListingDetailScreen(
                viewModel = detailViewModel
            )
        }
    }
}
