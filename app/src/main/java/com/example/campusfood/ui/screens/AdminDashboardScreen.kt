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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusfood.model.OrderResponse
import com.example.campusfood.ui.components.EmptyState
import com.example.campusfood.ui.components.ErrorState
import com.example.campusfood.ui.components.ShimmerOrderCard
import com.example.campusfood.ui.components.StatusBadge
import com.example.campusfood.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onManageProducts: () -> Unit = {}
) {
    val ordersState by adminViewModel.ordersState.collectAsState()
    val statusUpdateLoading by adminViewModel.statusUpdateLoading.collectAsState()
    val snackbarEvent by adminViewModel.snackbarEvent.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle admin snackbar events
    LaunchedEffect(snackbarEvent) {
        snackbarEvent?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            adminViewModel.clearSnackbarEvent()
        }
    }

    val filters = listOf("ALL", "PLACED", "PACKING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED")

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Premium admin header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AdminPurple, AdminPurpleDark, Color(0xFF311B92))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Manage orders & products",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 10.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledIconButton(
                            onClick = onManageProducts,
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Inventory2, "Products", modifier = Modifier.size(18.dp))
                        }
                        FilledIconButton(
                            onClick = { adminViewModel.loadAllOrders() },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
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
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Stats bar
            if (ordersState is AdminOrdersState.Success) {
                val orders = (ordersState as AdminOrdersState.Success).orders
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    data class StatInfo(val label: String, val count: Int, val icon: ImageVector, val color: Color)
                    val stats = listOf(
                        StatInfo("Total", orders.size, Icons.Default.GridView, AdminPurple),
                        StatInfo("Placed", orders.count { it.status == "PLACED" }, Icons.Default.Pending, BlueInfo),
                        StatInfo("Packing", orders.count { it.status == "PACKING" }, Icons.Default.Inventory2, AmberWarning),
                        StatInfo("Delivering", orders.count { it.status == "OUT_FOR_DELIVERY" }, Icons.Default.DeliveryDining, OrangePrimary),
                        StatInfo("Done", orders.count { it.status == "DELIVERED" }, Icons.Default.CheckCircle, GreenSuccess)
                    )
                    items(stats) { stat ->
                        StatCard(label = stat.label, count = stat.count, icon = stat.icon, accentColor = stat.color)
                    }
                }
            }

            // Filter chips – larger
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(32.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdminPurple,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent,
                            enabled = true,
                            selected = selectedFilter == filter
                        )
                    )
                }
            }

            // Order list
            when (val state = ordersState) {
                is AdminOrdersState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(4) {
                            ShimmerOrderCard()
                        }
                    }
                }
                is AdminOrdersState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { adminViewModel.loadAllOrders() },
                        emoji = "😕",
                        title = "Failed to load orders"
                    )
                }
                is AdminOrdersState.Success -> {
                    val filtered = if (selectedFilter == "ALL") state.orders
                    else state.orders.filter { it.status == selectedFilter }

                    if (filtered.isEmpty()) {
                        EmptyState(
                            emoji = "📋",
                            title = "No orders with this status",
                            subtitle = "Orders will appear here when their status matches."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            item { Spacer(Modifier.height(12.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, count: Int, icon: ImageVector, accentColor: Color) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(6.dp),
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = accentColor
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        order.userName ?: "User #${order.userId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (order.createdAt != null) {
                        Text(
                            text = formatAdminDateTime(order.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.sp
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
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                    fontSize = 13.sp
                )
            }

            if (!order.deliveryAddress.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        "📍 ${order.deliveryAddress}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Total + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₹${String.format(java.util.Locale.getDefault(), "%.2f", order.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = OrangePrimary,
                    fontSize = 20.sp
                )

                if (order.status != "DELIVERED" && order.status != "CANCELLED") {
                    Box {
                        Button(
                            onClick = { showStatusMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AdminPurple
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(38.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            enabled = !isUpdating
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Update,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Update", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                                            fontSize = 14.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            getStatusIcon(status),
                                            null,
                                            tint = getStatusColor(status),
                                            modifier = Modifier.size(18.dp)
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
