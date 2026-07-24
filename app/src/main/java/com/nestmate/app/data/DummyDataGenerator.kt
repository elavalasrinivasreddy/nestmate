package com.nestmate.app.data

import com.nestmate.app.data.model.Listing
import com.nestmate.app.data.model.Location
import com.nestmate.app.data.model.RoomType
import com.nestmate.app.data.model.UserProfile
import java.util.UUID

object DummyDataGenerator {

    fun generateDummyProfiles(count: Int = 5): List<UserProfile> {
        val names = listOf("Alex Sharma", "Priya Patel", "Rohan Singh", "Sneha Gupta", "Karan Malhotra", "Aisha Khan")
        return (1..count).map {
            val id = UUID.randomUUID().toString()
            UserProfile(
                uid = id,
                displayName = names.random(),
                photoUrl = "https://i.pravatar.cc/300?u=$id",
                bio = "Looking for a chill roommate! I'm clean and mostly work during the week."
            )
        }
    }

    fun generateDummyListings(count: Int = 10, ownerProfiles: List<UserProfile> = generateDummyProfiles()): List<Listing> {
        val titles = listOf(
            "Spacious Room in 3BHK",
            "Cozy Private Room near Metro",
            "Looking for flatmate for Shared Room",
            "Fully Furnished Master Bedroom"
        )
        val areas = listOf("Koramangala", "Indiranagar", "HSR Layout", "Whitefield", "Jayanagar")

        return (1..count).map {
            val id = UUID.randomUUID().toString()
            val owner = ownerProfiles.random()
            Listing(
                id = id,
                ownerUid = owner.uid,
                title = titles.random(),
                description = "Great apartment with lots of natural light. Looking for someone friendly and tidy.",
                roomType = RoomType.values().random(),
                rentAmount = (10000..30000).random().toDouble(),
                location = Location(city = "Bangalore", area = areas.random()),
                // Using picsum.photos with a seed ensures we get a consistent but random room-like image
                imageUrls = listOf(
                    "https://picsum.photos/seed/${id}_1/600/400",
                    "https://picsum.photos/seed/${id}_2/600/400"
                )
            )
        }
    }
}
