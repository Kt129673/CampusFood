package com.example.campusfood.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun OrderScreen(
    viewModel: OrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarEvent by viewModel.snackbarEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-shot snackbar events
    LaunchedEffect(snackbarEvent) {
        snackbarEvent?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSnackbarEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = Color.Transparent,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(OrangePrimary, OrangePrimaryDark, Color(0xFFBF360C))
                            )
                        )
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
                                "My Orders",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Track your food orders",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        // Order count badge
                        if (uiState is OrderUiState.Success) {
                            val count = (uiState as OrderUiState.Success).orders.size
                            if (count > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        "$count",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is OrderUiState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(4) {
                            ShimmerOrderCard()
                        }
                    }
                }
                is OrderUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.getOrders() },
                        emoji = "📦",
                        title = "Couldn't load orders"
                    )
                }
                is OrderUiState.Success -> {
                    if (state.orders.isEmpty()) {
                        EmptyState(
                            emoji = "🛒",
                            title = "No orders yet",
                            subtitle = "Your order history will appear here once you place your first order.",
                            actionText = null
                        )
                    } else {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refreshOrders() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 10.dp,
                                    bottom = 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.orders, key = { it.id }) { order ->
                                    OrderCard(order = order, viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderResponse,
    viewModel: OrderViewModel
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Icon(Icons.Default.Cancel, null, tint = RedError) },
            title = { Text("Cancel Order", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel order #${order.id}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelOrder(order.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel Order", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Keep Order")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: order ID + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Order #${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!order.createdAt.isNullOrBlank()) {
                        Text(
                            formatOrderDate(order.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items list with quantity badges
            order.items?.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(22.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = OrangePrimary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${item.quantity}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            item.productName ?: "Item #${item.productId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    if (item.totalPrice != null) {
                        Text(
                            "₹${String.format(Locale.getDefault(), "%.0f", item.totalPrice)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(8.dp))

            // Total + action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        "₹${String.format(Locale.getDefault(), "%.0f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangePrimary,
                        fontSize = 18.sp
                    )
                }
                // Only PLACED orders can be cancelled
                if (order.status == "PLACED") {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RedError
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Delivery address
            if (!order.deliveryAddress.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        order.deliveryAddress,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Formats an ISO datetime string to a more readable format.
 */
private fun formatOrderDate(isoString: String): String {
    return try {
        val parts = isoString.split("T")
        val date = parts[0]
        val timeParts = parts.getOrNull(1)?.split(":")
        val hour = timeParts?.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = timeParts?.getOrNull(1) ?: "00"
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val dateParts = date.split("-")
        val months = listOf(
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthIndex = dateParts.getOrNull(1)?.toIntOrNull() ?: 0
        val monthName = months.getOrElse(monthIndex) { dateParts.getOrElse(1) { "" } }
        "${dateParts.getOrElse(2) { "" }} $monthName · $displayHour:$minute $amPm"
    } catch (_: Exception) {
        isoString.split("T").getOrElse(0) { isoString }
    }
}
