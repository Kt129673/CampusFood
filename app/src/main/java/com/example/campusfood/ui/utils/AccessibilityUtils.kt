package com.example.campusfood.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.view.accessibility.AccessibilityManager
import java.util.Locale

/**
 * Accessibility utilities for enhanced screen reader support.
 */

/**
 * Checks if TalkBack or other screen readers are enabled.
 */
@Composable
fun isScreenReaderEnabled(): Boolean {
    val context = LocalContext.current
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    return (accessibilityManager?.isEnabled == true) && (accessibilityManager.isTouchExplorationEnabled == true)
}

/**
 * Enhanced clickable modifier with better accessibility support.
 * Automatically adds role and state descriptions.
 */
fun Modifier.accessibleClickable(
    label: String,
    role: Role = Role.Button,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    this.semantics {
        this.role = role
        this.contentDescription = label
        if (!enabled) disabled()
    }
    .clickable(
        enabled = enabled,
        onClick = onClick,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    )
}

/**
 * Adds semantic properties for better screen reader announcements.
 */
fun Modifier.accessibleAction(
    label: String,
    action: () -> Boolean
): Modifier = this.semantics {
    customActions = listOf(
        CustomAccessibilityAction(label) {
            action()
        }
    )
}

/**
 * Marks content as heading for better navigation.
 */
fun Modifier.heading(): Modifier = this.semantics {
    heading()
}

/**
 * Groups related content for screen readers.
 */
fun Modifier.accessibilityGroup(
    description: String? = null
): Modifier = this.semantics(mergeDescendants = true) {
    description?.let { contentDescription = it }
}

/**
 * Announces live updates to screen readers.
 */
fun Modifier.liveRegion(
    priority: LiveRegionMode = LiveRegionMode.Polite
): Modifier = this.semantics {
    liveRegion = priority
}

/**
 * Formats price for screen reader announcement.
 */
fun formatPriceForAccessibility(price: Double): String {
    return "₹${String.format(Locale.getDefault(), "%.0f", price)} rupees"
}

/**
 * Formats quantity for screen reader announcement.
 */
fun formatQuantityForAccessibility(quantity: Int, itemName: String): String {
    return if (quantity == 1) {
        "1 $itemName"
    } else {
        "$quantity ${itemName}s"
    }
}

/**
 * Formats order status for screen reader announcement.
 */
fun formatStatusForAccessibility(status: String): String {
    return when (status.uppercase()) {
        "PLACED" -> "Order placed, awaiting confirmation"
        "CONFIRMED" -> "Order confirmed, being prepared"
        "PREPARING" -> "Order is being prepared"
        "OUT_FOR_DELIVERY" -> "Order is out for delivery"
        "DELIVERED" -> "Order has been delivered"
        "CANCELLED" -> "Order was cancelled"
        else -> status
    }
}

/**
 * Creates a descriptive label for product cards.
 */
fun createProductAccessibilityLabel(
    name: String,
    category: String,
    price: Double,
    stock: Int?,
    description: String?
): String = buildString {
    append(name)
    append(", ")
    append(category)
    append(", ")
    append(formatPriceForAccessibility(price))
    
    when {
        stock == null -> {}
        stock <= 0 -> append(", Out of stock")
        stock <= 10 -> append(", Only $stock left in stock")
    }
    
    if (!description.isNullOrBlank()) {
        append(", ")
        append(description)
    }
}

/**
 * Creates a descriptive label for cart items.
 */
fun createCartItemAccessibilityLabel(
    name: String,
    quantity: Int,
    price: Double,
    totalPrice: Double
): String = buildString {
    append(formatQuantityForAccessibility(quantity, name))
    append(", ")
    append(formatPriceForAccessibility(price))
    append(" each, ")
    append("Total: ")
    append(formatPriceForAccessibility(totalPrice))
}

/**
 * Minimum touch target size for accessibility (48dp).
 */
const val MIN_TOUCH_TARGET_SIZE = 48

/**
 * Recommended text contrast ratios.
 */
object ContrastRatios {
    const val NORMAL_TEXT_AA = 4.5  // WCAG AA for normal text
    const val LARGE_TEXT_AA = 3.0   // WCAG AA for large text (18sp+)
    const val NORMAL_TEXT_AAA = 7.0 // WCAG AAA for normal text
    const val LARGE_TEXT_AAA = 4.5  // WCAG AAA for large text
}
