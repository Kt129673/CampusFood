package com.example.campusfood.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.ui.theme.OrangePrimary

/**
 * Filter chips for order status filtering.
 */
@Composable
fun OrderFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "All" to "All Orders",
        "PLACED" to "Placed",
        "PREPARING" to "Preparing",
        "OUT_FOR_DELIVERY" to "Out for Delivery",
        "DELIVERED" to "Delivered",
        "CANCELLED" to "Cancelled"
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (value, label) ->
            FilterChip(
                selected = selectedFilter == value,
                onClick = { onFilterSelected(value) },
                label = {
                    Text(
                        label,
                        fontWeight = if (selectedFilter == value) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = if (selectedFilter == value) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}
