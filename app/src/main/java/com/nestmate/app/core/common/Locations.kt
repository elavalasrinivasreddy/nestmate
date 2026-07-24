package com.nestmate.app.core.common

/**
 * Curated location catalog for validated area/city selection (no external
 * Maps/Places API). Keeps posting + search consistent and typo-free.
 * Extend as coverage grows; Bangalore localities are seeded first since the
 * initial market is Bangalore.
 */
object Locations {

    val cities: List<String> = listOf(
        "Bangalore", "Hyderabad", "Chennai", "Mumbai", "Pune",
        "Delhi", "Gurgaon", "Noida", "Kolkata", "Ahmedabad"
    )

    /** area -> city, so a selected area implies (and validates) its city. */
    val areasByCity: Map<String, List<String>> = mapOf(
        "Bangalore" to listOf(
            "Koramangala", "Indiranagar", "HSR Layout", "Whitefield", "Jayanagar",
            "BTM Layout", "Marathahalli", "Bellandur", "JP Nagar", "Electronic City",
            "Hebbal", "Yelahanka", "Rajajinagar", "Malleshwaram", "Sarjapur Road"
        ),
        "Hyderabad" to listOf("Gachibowli", "Madhapur", "Hitech City", "Kondapur", "Kukatpally", "Banjara Hills"),
        "Chennai" to listOf("Adyar", "Velachery", "T. Nagar", "OMR", "Anna Nagar"),
        "Mumbai" to listOf("Andheri", "Bandra", "Powai", "Thane", "Malad"),
        "Pune" to listOf("Hinjewadi", "Kothrud", "Baner", "Viman Nagar", "Wakad"),
        "Delhi" to listOf("Saket", "Dwarka", "Rohini", "Lajpat Nagar"),
        "Gurgaon" to listOf("DLF Phase 1", "Sohna Road", "Cyber City", "Golf Course Road"),
        "Noida" to listOf("Sector 62", "Sector 18", "Sector 137"),
        "Kolkata" to listOf("Salt Lake", "New Town", "Ballygunge"),
        "Ahmedabad" to listOf("Satellite", "Bopal", "Prahlad Nagar")
    )

    /** All known areas, flattened — for autocomplete + validation. */
    val allAreas: List<String> = areasByCity.values.flatten()

    fun cityForArea(area: String): String? =
        areasByCity.entries.firstOrNull { it.value.any { a -> a.equals(area, ignoreCase = true) } }?.key

    fun isValidArea(area: String): Boolean =
        allAreas.any { it.equals(area.trim(), ignoreCase = true) }
}
