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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.campusfood.model.CartItem
import com.example.campusfood.ui.theme.GreenSuccess
import com.example.campusfood.ui.theme.OrangePrimary
import com.example.campusfood.ui.theme.OrangePrimaryDark
import com.example.campusfood.ui.theme.OrangePrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckoutClick: (String) -> Unit,
    onBackToMenu: () -> Unit,
    viewModel: CartViewModel,
    isPlacingOrder: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var deliveryAddress by remember { mutableStateOf("Campus Dorm A, Room 101") }

    Scaffold(
        topBar = {
            // Premium gradient header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
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
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "My Cart",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            if (uiState is CartUiState.Success) {
                                val items = (uiState as CartUiState.Success).items
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    if (items.isEmpty()) "Your basket is empty" else "${items.sumOf { it.quantity }} items selected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (uiState is CartUiState.Success && (uiState as CartUiState.Success).items.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = { viewModel.clearCart() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Clear", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    val subtotal = items.sumOf { it.price * it.quantity }
                    val deliveryFee = 20.0
                    val total = subtotal + deliveryFee

                    Surface(
                        shadowElevation = 24.dp,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 20.dp)
                                .navigationBarsPadding()
                        ) {
                            // Price Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Fee", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%.2f", deliveryFee)}", style = MaterialTheme.typography.bodyLarge, color = GreenSuccess, fontWeight = FontWeight.Medium)
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "₹${String.format("%.2f", total)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

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
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    focusedLabelColor = OrangePrimary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { onCheckoutClick(deliveryAddress) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                enabled = deliveryAddress.isNotBlank() && !isPlacingOrder
                            ) {
                                if (isPlacingOrder) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                                } else {
                                    Text(
                                        "Place Order • ₹${String.format("%.2f", total)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is CartUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(48.dp), color = OrangePrimary, strokeWidth = 4.dp)
                }
                is CartUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                }
                is CartUiState.Success -> {
                    if (state.items.isEmpty()) {
                        EmptyCartState(onBackToMenu)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(state.items, key = { it.productId }) { item ->
                                CartItemCard(
                                    item = item,
                                    onIncrement = { viewModel.incrementQuantity(item.productId) },
                                    onDecrement = { viewModel.decrementQuantity(item.productId) },
                                    onRemove = { viewModel.removeFromCart(item.productId) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartState(onBackToMenu: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(140.dp),
            shape = RoundedCornerShape(70.dp),
            color = OrangePrimary.copy(alpha = 0.08f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🛒", fontSize = 56.sp)
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            "Your cart is empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Looks like you haven't added anything to your cart yet.\nGo ahead and explore our menu!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = onBackToMenu,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
            modifier = Modifier.height(52.dp).fillMaxWidth(0.7f),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
            Text("Browse Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image – larger with rounded corners
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl?.ifBlank { null })
                        .crossfade(true)
                        .build(),
                    contentDescription = item.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OrangePrimary.copy(alpha = 0.4f),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            OrangePrimary.copy(alpha = 0.06f),
                                            OrangePrimaryLight.copy(alpha = 0.12f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = OrangePrimary.copy(alpha = 0.35f)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        item.category?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelMedium,
                                color = OrangePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Premium quantity stepper
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = onDecrement,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(18.dp), tint = OrangePrimary)
                            }
                            Text(
                                "${item.quantity}",
                                modifier = Modifier.padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = onIncrement,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(18.dp), tint = OrangePrimary)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "₹${String.format("%.2f", item.price * item.quantity)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangePrimary,
                            fontSize = 18.sp
                        )
                        if (item.quantity > 1) {
                            Text(
                                "₹${String.format("%.0f", item.price)} each",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

