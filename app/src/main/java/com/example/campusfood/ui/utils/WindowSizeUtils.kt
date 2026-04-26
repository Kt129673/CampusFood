package com.example.campusfood.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size class utilities for adaptive layouts.
 * Helps create responsive designs for phones, tablets, and foldables.
 */

enum class WindowSizeClass {
    COMPACT,    // Phone in portrait (< 600dp)
    MEDIUM,     // Tablet in portrait or phone in landscape (600-840dp)
    EXPANDED    // Tablet in landscape (> 840dp)
}

/**
 * Determines the current window size class based on screen width.
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 600.dp -> WindowSizeClass.COMPACT
        screenWidth < 840.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Adaptive spacing based on window size.
 */
@Composable
fun adaptiveSpacing(
    compact: Dp = 16.dp,
    medium: Dp = 24.dp,
    expanded: Dp = 32.dp
): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> compact
        WindowSizeClass.MEDIUM -> medium
        WindowSizeClass.EXPANDED -> expanded
    }
}

/**
 * Adaptive column count for grid layouts.
 */
@Composable
fun adaptiveColumnCount(
    compact: Int = 1,
    medium: Int = 2,
    expanded: Int = 3
): Int {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> compact
        WindowSizeClass.MEDIUM -> medium
        WindowSizeClass.EXPANDED -> expanded
    }
}

/**
 * Maximum content width for large screens.
 */
@Composable
fun maxContentWidth(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> Dp.Infinity
        WindowSizeClass.MEDIUM -> 720.dp
        WindowSizeClass.EXPANDED -> 1200.dp
    }
}

/**
 * Check if device is in landscape orientation.
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}
