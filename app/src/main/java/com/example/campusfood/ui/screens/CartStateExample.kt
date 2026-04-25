package com.example.campusfood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusfood.model.Product
import com.example.campusfood.ui.theme.OrangePrimary

/**
 * Example demonstrating proper state management with ViewModel and StateFlow.
 * 
 * This example shows:
 * 1. How to collect state from ViewModel
 * 2. How UI automatically updates when state changes
 * 3. How to handle user actions
 * 4. How derived states (cart count, total) work
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartStateExample(
    cartViewModel: CartViewModel = viewModel()
) {
    // Collect states - UI updates automatically when these change
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
    val totalAmount by cartViewModel.totalAmount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Cart State Management Example")
                        Text(
                            "Items: $cartItemCount | Total: ₹${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangePrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // State indicator
            StateIndicatorCard(
                cartItemCount = cartItemCount,
                totalAmount = totalAmount
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cart items
            when (val state = cartState) {
                is CartUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is CartUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is CartUiState.Success -> {
                    if (state.items.isEmpty()) {
                        EmptyCartView(onAddSampleItems = {
                            addSampleProducts(cartViewModel)
                        })
                    } else {
                        CartItemsList(
                            items = state.items,
                            onIncrement = { cartViewModel.incrementQuantity(it) },
                            onDecrement = { cartViewModel.decrementQuantity(it) },
                            onRemove = { cartViewModel.removeFromCart(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StateIndicatorCard(
    cartItemCount: Int,
    totalAmount: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = OrangePrimary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "🔄 State Updates Automatically",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Cart Count: $cartItemCount (derived from StateFlow)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Total Amount: ₹${String.format("%.2f", totalAmount)} (derived from StateFlow)",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "These values update automatically when you add/remove items!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyCartView(
    onAddSampleItems: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Cart is Empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Add some sample items to see state management in action",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddSampleItems,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Sample Items")
        }
    }
}

@Composable
private fun CartItemsList(
    items: List<com.example.campusfood.model.CartItem>,
    onIncrement: (Long) -> Unit,
    onDecrement: (Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.productId }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "₹${String.format("%.2f", item.price)} each",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Total: ₹${String.format("%.2f", item.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Quantity controls
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            IconButton(
                                onClick = { onDecrement(item.productId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = OrangePrimary
                                )
                            }
                            Text(
                                "${item.quantity}",
                                modifier = Modifier.padding(horizontal = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { onIncrement(item.productId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = OrangePrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to add sample products for demonstration.
 */
private fun addSampleProducts(cartViewModel: CartViewModel) {
    val sampleProducts = listOf(
        Product(
            id = 1,
            name = "Samosa",
            description = "Crispy and delicious",
            price = 20.0,
            category = "Snacks",
            imageUrl = null,
            stock = 50
        ),
        Product(
            id = 2,
            name = "Chai",
            description = "Hot tea",
            price = 15.0,
            category = "Beverages",
            imageUrl = null,
            stock = 100
        ),
        Product(
            id = 3,
            name = "Sandwich",
            description = "Veg sandwich",
            price = 40.0,
            category = "Quick Bites",
            imageUrl = null,
            stock = 30
        )
    )

    sampleProducts.forEach { product ->
        cartViewModel.addToCart(product)
    }
}
