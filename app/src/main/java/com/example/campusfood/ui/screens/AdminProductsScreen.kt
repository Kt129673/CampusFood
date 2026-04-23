package com.example.campusfood.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.campusfood.model.Product
import com.example.campusfood.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    adminViewModel: AdminViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    onBack: () -> Unit
) {
    val productsState by adminViewModel.productsState.collectAsState()
    val productActionState by adminViewModel.productActionState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
    var showInventoryDialog by remember { mutableStateOf<Product?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on action result
    LaunchedEffect(productActionState) {
        when (productActionState) {
            is AdminProductActionState.Success -> {
                snackbarHostState.showSnackbar(
                    (productActionState as AdminProductActionState.Success).message
                )
                adminViewModel.clearProductAction()
            }
            is AdminProductActionState.Error -> {
                snackbarHostState.showSnackbar(
                    (productActionState as AdminProductActionState.Error).message
                )
                adminViewModel.clearProductAction()
            }
            else -> {}
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(Icons.Default.Delete, null, tint = RedError)
            },
            title = {
                Text("Delete Product", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete \"${product.name}\"? This will make it unavailable.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteProduct(product.id ?: 0)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Inventory update dialog
    showInventoryDialog?.let { product ->
        var stockText by remember { mutableStateOf("${product.stock ?: 0}") }
        AlertDialog(
            onDismissRequest = { showInventoryDialog = null },
            icon = {
                Icon(Icons.Default.Inventory, null, tint = Color(0xFF7B1FA2))
            },
            title = {
                Text("Update Stock", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) stockText = it },
                        label = { Text("Quantity") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7B1FA2),
                            focusedLabelColor = Color(0xFF7B1FA2)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = stockText.toIntOrNull() ?: 0
                        adminViewModel.updateInventory(product.id ?: 0, qty)
                        showInventoryDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Text("Update", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showInventoryDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = Color.Transparent,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))))
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Manage Products",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Add, edit, stock management",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { adminViewModel.loadAllProducts() },
                            modifier = Modifier.size(34.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Refresh, "Refresh", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = Color(0xFF7B1FA2),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Product")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search products...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(18.dp))
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7B1FA2),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            when (val state = productsState) {
                is AdminProductsState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7B1FA2))
                    }
                }
                is AdminProductsState.Error -> {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😕", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(onClick = { adminViewModel.loadAllProducts() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is AdminProductsState.Success -> {
                    val filtered = state.products.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📦", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (searchQuery.isNotEmpty()) "No matching products" else "No products yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered, key = { it.id ?: 0 }) { product ->
                                AdminProductCard(
                                    product = product,
                                    onEdit = { onEditProduct(product.id ?: 0) },
                                    onDelete = { showDeleteDialog = product },
                                    onUpdateStock = { showInventoryDialog = product }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            product.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "₹${String.format("%.0f", product.price)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    product.category,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF7B1FA2),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stock info
                    Surface(
                        onClick = onUpdateStock,
                        shape = RoundedCornerShape(8.dp),
                        color = if ((product.stock ?: 0) <= 5) RedError.copy(alpha = 0.08f)
                                else GreenSuccess.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                null,
                                modifier = Modifier.size(12.dp),
                                tint = if ((product.stock ?: 0) <= 5) RedError else GreenSuccess
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "Stock: ${product.stock ?: 0}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if ((product.stock ?: 0) <= 5) RedError else GreenSuccess,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Edit / Delete buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                "Edit",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                tint = RedError.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
