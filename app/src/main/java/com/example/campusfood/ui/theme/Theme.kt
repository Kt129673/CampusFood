package com.example.campusfood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangePrimaryDark,
    onPrimaryContainer = OrangeTertiary,
    secondary = OrangeSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3D2E00),
    onSecondaryContainer = OrangeTertiary,
    tertiary = OrangeTertiary,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    error = RedError,
    onError = Color.White,
    outline = Color(0xFF3A3A3A)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeTertiary,
    onPrimaryContainer = OrangePrimaryDark,
    secondary = OrangeSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFE8CC),
    onSecondaryContainer = Color(0xFF3D2E00),
    tertiary = OrangeTertiary,
    onTertiary = Color.Black,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    error = RedError,
    onError = Color.White,
    outline = Color(0xFFE0E0E0)
)

@Composable
fun CampusFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Use our custom color scheme — no dynamic color to enforce brand consistency
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}