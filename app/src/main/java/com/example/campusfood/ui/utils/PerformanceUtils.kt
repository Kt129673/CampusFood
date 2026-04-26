package com.example.campusfood.ui.utils

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.delay

/**
 * Performance monitoring utilities for Compose UI.
 * Helps track recomposition and identify performance bottlenecks.
 */

/**
 * Tracks and logs recomposition count for debugging.
 * Use in development to identify unnecessary recompositions.
 * 
 * Usage:
 * ```
 * val recompositions = rememberRecompositionCount("MyScreen")
 * Text("Recomposed $recompositions times")
 * ```
 */
@Composable
fun rememberRecompositionCount(tag: String = "Composable"): Int {
    val count = remember { mutableStateOf(0) }
    
    SideEffect {
        count.value++
        if (count.value > 1) {
            println("🔄 Recomposition #${count.value}: $tag")
        }
    }
    
    return count.value
}

/**
 * Debounces state changes to reduce recompositions.
 * Useful for search inputs and other frequently changing values.
 * 
 * Usage:
 * ```
 * val debouncedQuery = rememberDebouncedValue(searchQuery, delayMillis = 300)
 * LaunchedEffect(debouncedQuery) {
 *     performSearch(debouncedQuery)
 * }
 * ```
 */
@Composable
fun <T> rememberDebouncedValue(
    value: T,
    delayMillis: Long = 300L
): T {
    val debouncedValue = remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        delay(delayMillis)
        debouncedValue.value = value
    }
    
    return debouncedValue.value
}

/**
 * Stable wrapper for lambda functions to prevent unnecessary recompositions.
 * Use when passing callbacks to child composables.
 * 
 * Usage:
 * ```
 * val onClick = rememberStableCallback { item ->
 *     viewModel.addToCart(item)
 * }
 * ```
 */
@Composable
fun <T> rememberStableCallback(callback: (T) -> Unit): (T) -> Unit {
    val currentCallback by rememberUpdatedState(callback)
    return remember {
        { value: T -> currentCallback(value) }
    }
}

/**
 * Modifier that measures and logs layout performance.
 * Use in development to identify slow layouts.
 */
fun Modifier.measurePerformance(tag: String): Modifier = this.layout { measurable, constraints ->
    val startTime = System.nanoTime()
    val placeable = measurable.measure(constraints)
    val measureTime = (System.nanoTime() - startTime) / 1_000_000.0
    
    if (measureTime > 16.0) { // Slower than 60fps frame time
        println("⚠️ Slow layout: $tag took ${measureTime}ms")
    }
    
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}

/**
 * Immutable data class wrapper to ensure stability.
 * Use for complex objects passed to composables.
 */
@Immutable
data class StableHolder<T>(val value: T)

/**
 * Creates a stable reference to a value.
 */
@Composable
fun <T> rememberStable(value: T): StableHolder<T> {
    return remember(value) { StableHolder(value) }
}
