package com.nestmate.app.core.navigation

/**
 * Top-level navigation destinations. New routes are added per phase
 * (auth, home, listings, requirements, chat, bookmarks, …).
 */
sealed class Destination(val route: String) {
    data object Welcome : Destination("welcome")
}
