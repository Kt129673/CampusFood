package com.example.campusfood.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusfood.ui.theme.*

/**
 * Reusable status badge component for order status display.
 * Color-coded to match backend OrderStatus enum values.
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

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
