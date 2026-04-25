package com.example.campusfood.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusfood.model.Product
import com.example.campusfood.ui.components.EmptyState
import com.example.campusfood.ui.components.ErrorState
import com.example.campusfood.ui.components.ProductCard
import com.example.campusfood.ui.components.ShimmerProductCard
import com.example.campusfood.ui.theme.OrangePrimary
import com.example.campusfood.ui.theme.OrangePrimaryDark
import com.example.campusfood.ui.theme.OrangePrimaryLight
import com.example.campusfood.ui.theme.GreenSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isBackendOnline.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Categories matching the seeded backend data
    val categories = listOf("All", "Snacks", "Beverages", "Hot Beverages", "Quick Bites", "Essentials", "Chocolates")
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column {
                    // Premium compact header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        OrangePrimary,
                                        OrangePrimaryDark,
                                        Color(0xFFBF360C)
                                    )
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "ANISHA",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "CAMPUS FOOD",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Light,
                                        color = Color.White.copy(alpha = 0.9f),
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(
                                                if (isOnline) GreenSuccess else Color(0xFFF44336),
                                                shape = RoundedCornerShape(50)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (isOnline) "Fresh & fast delivery 🚀" else "Backend Offline ⚠️",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }

                            // Cart button – premium circular
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(
                                            containerColor = Color.White,
                                            contentColor = OrangePrimary
                                        ) {
                                            Text(
                                                "$cartItemCount",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                FilledIconButton(
                                    onClick = onCartClick,
                                    modifier = Modifier.size(42.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.18f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Premium search bar – taller, more spacious
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        placeholder = {
                            Text(
                                "Search food, snacks, drinks...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 15.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    // Category chips – slightly taller, better spacing
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        category,
                                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                },
                                leadingIcon = if (selectedCategory == category) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(38.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangePrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    selectedBorderColor = OrangePrimary,
                                    enabled = true,
                                    selected = selectedCategory == category
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is MenuUiState.Loading -> {
                // Shimmer skeleton loading – premium feel
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(5) {
                        ShimmerProductCard()
                    }
                }
            }
            is MenuUiState.Error -> {
                Box(modifier = Modifier.padding(innerPadding)) {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.getProducts() }
                    )
                }
            }
            is MenuUiState.Success -> {
                val filteredProducts = state.products.filter {
                    (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                            (it.name.contains(searchQuery, ignoreCase = true) ||
                             it.description?.contains(searchQuery, ignoreCase = true) == true)
                }

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        EmptyState(
                            emoji = "🔍",
                            title = "No items found",
                            subtitle = "Try a different search or category"
                        )
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            items(filteredProducts, key = { it.id ?: 0 }) { product ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut()
                                ) {
                                    ProductCard(
                                        product = product,
                                        onAddToCart = { onProductClick(it) }
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                        }
                    }
                }
            }
        }
    }
}
