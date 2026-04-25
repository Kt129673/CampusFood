package com.example.campusfood.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.campusfood.model.CartItem
import com.example.campusfood.ui.theme.GreenSuccess
import com.example.campusfood.ui.theme.OrangePrimary
import com.example.campusfood.ui.theme.OrangePrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckoutClick: (String) -> Unit,
    viewModel: CartViewModel,
    isPlacingOrder: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var deliveryAddress by remember { mutableStateOf("Campus Dorm A, Room 101") }

    Scaffold(
        topBar = {
            // Compact cart header
            Surface(
                color = Color.Transparent,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(OrangePrimary, OrangePrimaryDark)
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
                        Text(
                            "My Cart",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        if (uiState is CartUiState.Success) {
                            val items = (uiState as CartUiState.Success).items
                            if (items.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "${items.size} ${if (items.size == 1) "item" else "items"}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (uiState is CartUiState.Success) {
                val items = (uiState as CartUiState.Success).items
                if (items.isNotEmpty()) {
                    val total = items.sumOf { it.price * it.quantity }
                    // Sticky total section at bottom with elevation
                    Surface(
                        shadowElevation = 12.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .navigationBarsPadding()
                        ) {
                            // Order summary with large + bold total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Total Amount",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                    // Highlighted total price - large + bold
                                    Text(
                                        "₹${String.format(java.util.Locale.getDefault(), "%.2f", total)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp
                                    )
                                }
                                Text(
                                    "${items.sumOf { it.quantity }} items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Delivery address
                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Delivery Address", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    focusedLabelColor = OrangePrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Full-width "Place Order" button with elevation
                            Button(
                                onClick = { onCheckoutClick(deliveryAddress) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                ),
                                enabled = deliveryAddress.isNotBlank() && !isPlacingOrder
                            ) {
                                if (isPlacingOrder) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Placing Order...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Place Order",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
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
                is CartUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OrangePrimary
                    )
                }
                is CartUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                is CartUiState.Success -> {
                    if (state.items.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛒", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Your cart is empty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Browse the menu and add some\ndelicious items to get started!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.items, key = { it.productId }) { item ->
                                CartItemCard(
                                    item = item,
                                    onIncrement = { viewModel.incrementQuantity(item.productId) },
                                    onDecrement = { viewModel.decrementQuantity(item.productId) },
                                    onRemove = { viewModel.removeFromCart(item.productId) }
                                )
                            }
                            // Bottom spacing for checkout bar
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image with rounded corners (12-16dp)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl?.ifBlank { null })
                        .crossfade(true)
                        .build(),
                    contentDescription = item.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_report_image),
                    error = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Title - bold, 16sp+
                        Text(
                            item.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 16.sp
                        )
                        // Price per item - 12-14sp, gray
                        Text(
                            "₹${String.format(java.util.Locale.getDefault(), "%.0f", item.price)} each",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity controls and total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modern quantity stepper with + / - buttons
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            IconButton(
                                onClick = onDecrement,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(18.dp),
                                    tint = OrangePrimary
                                )
                            }
                            Text(
                                "${item.quantity}",
                                modifier = Modifier.padding(horizontal = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = onIncrement,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase",
                                    modifier = Modifier.size(18.dp),
                                    tint = OrangePrimary
                                )
                            }
                        }
                    }

                    // Item total - highlighted, semi-bold
                    Text(
                        "₹${String.format(java.util.Locale.getDefault(), "%.0f", item.price * item.quantity)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangePrimary,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
