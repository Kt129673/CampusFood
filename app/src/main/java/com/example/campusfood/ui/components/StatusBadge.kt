package com.example.campusfood.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.ui.theme.*

/**
 * Reusable status badge component for order status display.
 * Color-coded to match backend OrderStatus enum values.
 * Active statuses (not delivered/cancelled) pulse subtly for visual feedback.
 */
@Composable
fun StatusBadge(status: String) {
    val (icon, label, color) = when (status) {
        "PLACED" -> Triple(Icons.Default.Pending, "Placed", BlueInfo)
        "PACKING" -> Triple(Icons.Default.Inventory2, "Packing", AmberWarning)
        "OUT_FOR_DELIVERY" -> Triple(Icons.Default.DeliveryDining, "On the way", OrangePrimary)
        "DELIVERED" -> Triple(Icons.Default.CheckCircle, "Delivered", GreenSuccess)
        "CANCELLED" -> Triple(Icons.Default.Cancel, "Cancelled", RedError)
        else -> Triple(Icons.Default.Pending, status, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    // Active orders get a subtle pulse on the dot indicator
    val isActive = status in listOf("PLACED", "PACKING", "OUT_FOR_DELIVERY")
    val pulseScale = if (isActive) {
        val transition = rememberInfiniteTransition(label = "statusPulse")
        val scale by transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "statusPulseScale"
        )
        scale
    } else {
        1f
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isActive) {
                // Pulsing dot for active statuses
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .background(color, shape = CircleShape)
                )
                Spacer(Modifier.width(6.dp))
            }
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(Modifier.width(5.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 12.sp
            )
        }
    }
}
