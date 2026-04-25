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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.campusfood.model.Product
import com.example.campusfood.ui.components.EmptyState
import com.example.campusfood.ui.components.ErrorState
import com.example.campusfood.ui.components.ShimmerProductCard
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
                    colors = ButtonDefaults.buttonColors(containerColor = RedError),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
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
                Icon(Icons.Default.Inventory, null, tint = AdminPurple)
            },
            title = {
                Text("Update Stock", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) stockText = it },
                        label = { Text("Quantity") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminPurple,
                            focusedLabelColor = AdminPurple
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
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Update", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showInventoryDialog = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AdminPurple, AdminPurpleDark, Color(0xFF311B92))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Add, edit, stock management",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 10.sp
                        )
                    }
                    FilledIconButton(
                        onClick = { adminViewModel.loadAllProducts() },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = AdminPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, "Add Product", modifier = Modifier.size(24.dp))
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products...", fontSize = 15.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = AdminPurple, modifier = Modifier.size(22.dp))
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AdminPurple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            )

            when (val state = productsState) {
                is AdminProductsState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(5) {
                            ShimmerProductCard()
                        }
                    }
                }
                is AdminProductsState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { adminViewModel.loadAllProducts() },
                        emoji = "😕",
                        title = "Failed to load products"
                    )
                }
                is AdminProductsState.Success -> {
                    val filtered = state.products.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        EmptyState(
                            emoji = "📦",
                            title = if (searchQuery.isNotEmpty()) "No matching products" else "No products yet",
                            subtitle = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap + to add your first product"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image – larger
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangeAccentSoft)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl?.ifBlank { null })
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
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
                                modifier = Modifier.size(18.dp),
                                color = AdminPurple.copy(alpha = 0.4f),
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
                                            AdminPurple.copy(alpha = 0.06f),
                                            AdminPurpleLight.copy(alpha = 0.12f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Fastfood,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = AdminPurple.copy(alpha = 0.35f)
                            )
                        }
                    }
                )

                // Availability indicator overlay
                if (!product.available) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Hidden",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

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
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 15.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "₹${String.format(Locale.getDefault(), "%.0f", product.price)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AdminPurple.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    product.category,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AdminPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stock info
                    Surface(
                        onClick = onUpdateStock,
                        shape = RoundedCornerShape(10.dp),
                        color = if ((product.stock ?: 0) <= 5) RedError.copy(alpha = 0.06f)
                                else GreenSuccess.copy(alpha = 0.06f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = if ((product.stock ?: 0) <= 5) RedError else GreenSuccess
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Stock: ${product.stock ?: 0}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if ((product.stock ?: 0) <= 5) RedError else GreenSuccess,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Edit / Delete buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                "Edit",
                                tint = AdminPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                tint = RedError.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
