package com.example.campusfood.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SortOption {
    NEWEST_FIRST,
    OLDEST_FIRST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT
}

/**
 * Dropdown menu for sorting orders.
 */
@Composable
fun SortMenu(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilledTonalIconButton(
            onClick = { expanded = true }
        ) {
            Icon(Icons.Default.Sort, contentDescription = "Sort orders")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortMenuItem(
                icon = Icons.Default.ArrowDownward,
                text = "Newest First",
                selected = currentSort == SortOption.NEWEST_FIRST,
                onClick = {
                    onSortSelected(SortOption.NEWEST_FIRST)
                    expanded = false
                }
            )
            SortMenuItem(
                icon = Icons.Default.ArrowUpward,
                text = "Oldest First",
                selected = currentSort == SortOption.OLDEST_FIRST,
                onClick = {
                    onSortSelected(SortOption.OLDEST_FIRST)
                    expanded = false
                }
            )
            Divider()
            SortMenuItem(
                icon = Icons.Default.TrendingUp,
                text = "Highest Amount",
                selected = currentSort == SortOption.HIGHEST_AMOUNT,
                onClick = {
                    onSortSelected(SortOption.HIGHEST_AMOUNT)
                    expanded = false
                }
            )
            SortMenuItem(
                icon = Icons.Default.TrendingDown,
                text = "Lowest Amount",
                selected = currentSort == SortOption.LOWEST_AMOUNT,
                onClick = {
                    onSortSelected(SortOption.LOWEST_AMOUNT)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun SortMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
                if (selected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        onClick = onClick,
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    )
}
