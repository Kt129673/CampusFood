package com.example.campusfood.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.model.OrderResponse
import com.example.campusfood.ui.components.StatusBadge
import com.example.campusfood.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onManageProducts: () -> Unit = {}
) {
    val ordersState by adminViewModel.ordersState.collectAsState()
    val statusUpdateLoading by adminViewModel.statusUpdateLoading.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filters = listOf("ALL", "PLACED", "PACKING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED")

    Scaffold(
        topBar = {
            // Compact admin header
            Surface(
                color = Color.Transparent,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                "Manage orders & products",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Manage Products button
                            FilledTonalIconButton(
                                onClick = onManageProducts,
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Inventory2, "Products", modifier = Modifier.size(18.dp))
                            }
                            FilledTonalIconButton(
                                onClick = { adminViewModel.loadAllOrders() },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Refresh, "Refresh", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Compact stats bar
            if (ordersState is AdminOrdersState.Success) {
                val orders = (ordersState as AdminOrdersState.Success).orders
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stats = mapOf(
                        "Total" to orders.size,
                        "Placed" to orders.count { it.status == "PLACED" },
                        "Packing" to orders.count { it.status == "PACKING" },
                        "Delivering" to orders.count { it.status == "OUT_FOR_DELIVERY" },
                        "Done" to orders.count { it.status == "DELIVERED" }
                    )
                    items(stats.entries.toList()) { (label, count) ->
                        StatCard(label = label, count = count)
                    }
                }
            }

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                items(filters) { filter ->
                    val label = when (filter) {
                        "ALL" -> "All"
                        "PLACED" -> "Placed"
                        "PACKING" -> "Packing"
                        "OUT_FOR_DELIVERY" -> "Delivering"
                        "DELIVERED" -> "Delivered"
                        "CANCELLED" -> "Cancelled"
                        else -> filter
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(label, fontSize = 11.sp) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7B1FA2),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Order list
            when (val state = ordersState) {
                is AdminOrdersState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7B1FA2))
                    }
                }
                is AdminOrdersState.Error -> {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😕", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = { adminViewModel.loadAllOrders() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is AdminOrdersState.Success -> {
                    val filtered = if (selectedFilter == "ALL") state.orders
                    else state.orders.filter { it.status == selectedFilter }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📋", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "No orders with this status",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { order ->
                                AdminOrderCard(
                                    order = order,
                                    isUpdating = statusUpdateLoading == order.id,
                                    onStatusUpdate = { newStatus ->
                                        adminViewModel.updateOrderStatus(order.id, newStatus)
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, count: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF7B1FA2)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: OrderResponse,
    isUpdating: Boolean,
    onStatusUpdate: (String) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Order #${order.id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        order.userName ?: "User #${order.userId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (order.createdAt != null) {
                        Text(
                            text = formatAdminDateTime(order.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                    }
                }
                StatusBadge(status = order.status)
            }

            Spacer(Modifier.height(6.dp))

            // Items
            order.items?.forEach { item ->
                Text(
                    "• ${item.productName ?: "Product #${item.productId}"} × ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 1.dp),
                    fontSize = 11.sp
                )
            }

            if (!order.deliveryAddress.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "📍 ${order.deliveryAddress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )

            // Total + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₹${String.format(java.util.Locale.getDefault(), "%.2f", order.totalAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = OrangePrimary
                )

                if (order.status != "DELIVERED" && order.status != "CANCELLED") {
                    Box {
                        Button(
                            onClick = { showStatusMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp),
                            enabled = !isUpdating
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Update,
                                    null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Update", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            val nextStatuses = getNextStatuses(order.status)
                            nextStatuses.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            getStatusLabel(status),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            getStatusIcon(status),
                                            null,
                                            tint = getStatusColor(status),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        showStatusMenu = false
                                        onStatusUpdate(status)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Status transition logic matching backend state machine
private fun getNextStatuses(currentStatus: String): List<String> {
    return when (currentStatus) {
        "PLACED" -> listOf("PACKING", "CANCELLED")
        "PACKING" -> listOf("OUT_FOR_DELIVERY", "CANCELLED")
        "OUT_FOR_DELIVERY" -> listOf("DELIVERED")
        else -> emptyList()
    }
}

private fun getStatusLabel(status: String): String {
    return when (status) {
        "PLACED" -> "Placed"
        "PACKING" -> "Packing"
        "OUT_FOR_DELIVERY" -> "Out for Delivery"
        "DELIVERED" -> "Delivered"
        "CANCELLED" -> "Cancel Order"
        else -> status
    }
}

private fun getStatusIcon(status: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        "PLACED" -> Icons.Default.Pending
        "PACKING" -> Icons.Default.Inventory2
        "OUT_FOR_DELIVERY" -> Icons.Default.DeliveryDining
        "DELIVERED" -> Icons.Default.CheckCircle
        "CANCELLED" -> Icons.Default.Cancel
        else -> Icons.Default.Pending
    }
}

@Composable
private fun getStatusColor(status: String): Color {
    return when (status) {
        "PLACED" -> BlueInfo
        "PACKING" -> AmberWarning
        "OUT_FOR_DELIVERY" -> OrangePrimary
        "DELIVERED" -> GreenSuccess
        "CANCELLED" -> RedError
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Formats an ISO date string for admin dashboard display.
 */
private fun formatAdminDateTime(isoString: String): String {
    return try {
        val parts = isoString.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val time = parts[1].substringBefore(".").substring(0, 5)
            if (dateParts.size == 3) {
                val months = listOf(
                    "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                )
                val monthIndex = dateParts[1].toIntOrNull() ?: 0
                val monthName = months.getOrElse(monthIndex) { dateParts[1] }
                "${dateParts[2]} $monthName at $time"
            } else {
                "${parts[0]} at $time"
            }
        } else {
            isoString
        }
    } catch (_: Exception) {
        isoString
    }
}
