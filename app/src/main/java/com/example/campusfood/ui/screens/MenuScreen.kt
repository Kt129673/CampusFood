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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.rememberLazyListState
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
    var debouncedSearchQuery by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val networkMonitor = remember { com.example.campusfood.utils.NetworkMonitor(context) }
    val isNetworkConnected by networkMonitor.isConnected.collectAsState(initial = true)

    // Debounce search query to avoid excessive filtering
    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(300) // 300ms debounce
        debouncedSearchQuery = searchQuery
    }

    // Categories matching the seeded backend data
    val categories = listOf("All", "Snacks", "Beverages", "Hot Beverages", "Quick Bites", "Essentials", "Chocolates")
    var selectedCategory by remember { mutableStateOf("All") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Removed: auto-focus on entry was forcing keyboard open every time

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Column {
                // Network status banner
                com.example.campusfood.ui.components.NetworkStatusBanner(
                    isConnected = isNetworkConnected
                )
                
                // Gradient header - compact & premium
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "CAMPUS FOOD",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Light,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 0.8.sp,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            if (isOnline) GreenSuccess else Color(0xFFF44336),
                                            shape = RoundedCornerShape(50)
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isOnline) "Fresh & fast delivery 🚀" else "Backend Offline ⚠️",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Cart button
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
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            FilledIconButton(
                                onClick = onCartClick,
                                modifier = Modifier.size(34.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Search + Chips section - compact & premium
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .height(44.dp)
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text(
                                    "Search food, snacks, drinks...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search products",
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )

                        // Category chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            category,
                                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = if (selectedCategory == category) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    } else null,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.height(30.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangePrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = Color.Transparent,
                                        selectedBorderColor = Color.Transparent,
                                        enabled = true,
                                        selected = selectedCategory == category
                                    )
                                )
                            }
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
                    contentPadding = PaddingValues(vertical = 8.dp)
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
                            (it.name.contains(debouncedSearchQuery, ignoreCase = true) ||
                             it.description?.contains(debouncedSearchQuery, ignoreCase = true) == true)
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
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
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
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}
