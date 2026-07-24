package com.nestmate.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Rounded shape scale. M3 components read their shape from here — notably text
 * fields use [Shapes.extraSmall], so bumping it rounds every input (search,
 * forms, filters) app-wide instead of setting `shape =` on each field.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
