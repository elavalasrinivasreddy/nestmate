package com.nestmate.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nestmate.app.core.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = NestPrimaryLight,
    onPrimary = NestOnPrimaryLight,
    primaryContainer = NestPrimaryContainerLight,
    onPrimaryContainer = NestOnPrimaryContainerLight,
    secondary = NestSecondaryLight,
    onSecondary = NestOnSecondaryLight,
    secondaryContainer = NestSecondaryContainerLight,
    onSecondaryContainer = NestOnSecondaryContainerLight,
    tertiary = NestTertiaryLight,
    onTertiary = NestOnTertiaryLight,
    background = NestBackgroundLight,
    onBackground = NestOnBackgroundLight,
    surface = NestSurfaceLight,
    onSurface = NestOnSurfaceLight,
    surfaceVariant = NestSurfaceVariantLight,
    onSurfaceVariant = NestOnSurfaceVariantLight,
    outline = NestOutlineLight,
    error = NestErrorLight,
    onError = NestOnErrorLight,
)

private val DarkColors = darkColorScheme(
    primary = NestPrimaryDark,
    onPrimary = NestOnPrimaryDark,
    primaryContainer = NestPrimaryContainerDark,
    onPrimaryContainer = NestOnPrimaryContainerDark,
    secondary = NestSecondaryDark,
    onSecondary = NestOnSecondaryDark,
    secondaryContainer = NestSecondaryContainerDark,
    onSecondaryContainer = NestOnSecondaryContainerDark,
    tertiary = NestTertiaryDark,
    onTertiary = NestOnTertiaryDark,
    background = NestBackgroundDark,
    onBackground = NestOnBackgroundDark,
    surface = NestSurfaceDark,
    onSurface = NestOnSurfaceDark,
    surfaceVariant = NestSurfaceVariantDark,
    onSurfaceVariant = NestOnSurfaceVariantDark,
    outline = NestOutlineDark,
    error = NestErrorDark,
    onError = NestOnErrorDark,
)

/**
 * App theme. Uses the Nestmate brand palette by default. Set [dynamicColor] to
 * true to opt into Android 12+ wallpaper-based dynamic color instead.
 */
@Composable
fun NestmateTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
