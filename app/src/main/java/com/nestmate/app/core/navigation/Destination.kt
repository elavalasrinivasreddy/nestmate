package com.nestmate.app.core.navigation

/**
 * Top-level navigation destinations. New routes are added per phase
 * (profile, listings, requirements, chat, bookmarks, …).
 */
sealed class Destination(val route: String) {
    data object Welcome : Destination("welcome")
    data object Auth : Destination("auth")
    data object Home : Destination("home")
    data object Profile : Destination("profile")
    data class CreateListing(val id: String? = null) : Destination(
        if (id != null) "create_listing?id=$id" else "create_listing"
    ) {
        companion object {
            const val route = "create_listing?id={id}"
        }
    }
    data class ListingDetail(val id: String) : Destination("listing_detail/$id") {
        companion object {
            const val route = "listing_detail/{id}"
        }
    }
}
