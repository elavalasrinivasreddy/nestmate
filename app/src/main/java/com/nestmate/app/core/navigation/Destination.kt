package com.nestmate.app.core.navigation

/**
 * Top-level navigation destinations. New routes are added per phase
 * (profile, listings, requirements, chat, bookmarks, …).
 */
sealed class Destination(val route: String) {
    data object Welcome : Destination("welcome")
    data object Auth : Destination("auth")
    data object Home : Destination("home")
    data object PhoneVerify : Destination("phone_verify")
}
