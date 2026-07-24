package com.nestmate.app.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

/**
 * The Nestmate brand mark: a rounded-square in brand teal holding a simple
 * white "nest/home" glyph (a roof over a rounded nest). Reused across Welcome,
 * Auth, and mirrored by the adaptive launcher icon.
 */
@Composable
fun NestmateLogo(
    size: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    val mark = MaterialTheme.colorScheme.onPrimary
    val brand = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4))
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            // Roof: a triangle across the upper half.
            val roof = Path().apply {
                moveTo(w * 0.20f, h * 0.50f)
                lineTo(w * 0.50f, h * 0.24f)
                lineTo(w * 0.80f, h * 0.50f)
                close()
            }
            drawPath(roof, color = mark, style = Fill)
            // Nest: a rounded body beneath the roof.
            drawRoundRect(
                color = mark,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.48f),
                size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f, w * 0.06f)
            )
            // A small "egg" dot to read as a nest, punched in the brand color.
            drawCircle(
                color = brand,
                radius = w * 0.06f,
                center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.60f)
            )
        }
    }
}
