package com.nestmate.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nestmate.app.feature.welcome.WelcomeScreen

/**
 * Single source of navigation for the app. The graph grows phase by phase.
 */
@Composable
fun NestmateNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Welcome.route,
        modifier = modifier
    ) {
        composable(Destination.Welcome.route) {
            WelcomeScreen()
        }
    }
}
