package com.example.campusfood.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusfood.model.OrderItemRequest
import com.example.campusfood.model.OrderRequest
import com.example.campusfood.ui.screens.*
import com.example.campusfood.ui.theme.OrangePrimary
import com.example.campusfood.ui.theme.AdminPurple
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val isPlacingOrder by orderViewModel.isPlacingOrder.collectAsStateWithLifecycle()
    
    // Automatically updates when cart changes
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isAdmin = currentUser?.role == "ADMIN"
    
    // Show mobile number dialog if user logged in via Google without mobile
    var showMobileDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentUser?.mobile.isNullOrBlank() && currentUser?.role != "ADMIN") {
            showMobileDialog = true
        }
    }

    // Update OrderViewModel with real userId when user changes
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            orderViewModel.setUserId(user.id)
        }
    }

    // Handle navigation based on auth state - consolidated to avoid race conditions
    LaunchedEffect(authState, isLoggedIn) {
        when {
            // User logged out - navigate to login
            !isLoggedIn -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            // User just logged in successfully - navigate to appropriate screen
            authState is AuthUiState.Success -> {
                val user = (authState as AuthUiState.Success).user
                val dest = if (user.role == "ADMIN") Screen.AdminDashboard.route else Screen.Menu.route
                navController.navigate(dest) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }

    // Refresh orders when navigating to the Orders tab
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Orders.route) {
            orderViewModel.getOrders()
        }
    }

    // Determine if bottom bar should show (hide for admin sub-screens)
    val showBottomBar = isLoggedIn && currentRoute != Screen.Login.route
            && currentRoute != Screen.AdminProducts.route
            && currentRoute != Screen.AdminAddProduct.route
            && currentRoute != Screen.AdminEditProduct.route

    // Show mobile number dialog if user logged in via Google without mobile
    if (showMobileDialog && currentUser != null) {
        com.example.campusfood.ui.components.MobileNumberDialog(
            userName = currentUser?.name ?: "User",
            onConfirm = { mobile ->
                authViewModel.updateMobileNumber(mobile)
                showMobileDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Mobile number saved!",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            onDismiss = {
                showMobileDialog = false
            }
        )
    }

    // Navigation items based on role
    val bottomNavItems = if (isAdmin) {
        listOf(
            NavigationItem("Dashboard", Screen.AdminDashboard.route, Icons.Default.Dashboard, Icons.Outlined.Dashboard),
            NavigationItem("Menu", Screen.Menu.route, Icons.Default.Restaurant, Icons.Outlined.Restaurant),
            NavigationItem("Profile", Screen.Profile.route, Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
        )
    } else {
        listOf(
            NavigationItem("Menu", Screen.Menu.route, Icons.Default.Restaurant, Icons.Outlined.Restaurant),
            NavigationItem("Cart", Screen.Cart.route, Icons.Default.ShoppingCart, Icons.Outlined.ShoppingCart, cartItemCount),
            NavigationItem("Orders", Screen.Orders.route, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
            NavigationItem("Profile", Screen.Profile.route, Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(68.dp)
                    ) {
                        val currentDestination = navBackStackEntry?.destination
                        bottomNavItems.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (item.badgeCount > 0) {
                                                Badge(
                                                    containerColor = OrangePrimary,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        "${item.badgeCount}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.name,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        item.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (isAdmin) AdminPurple else OrangePrimary,
                                    selectedTextColor = if (isAdmin) AdminPurple else OrangePrimary,
                                    indicatorColor = if (isAdmin) AdminPurple.copy(alpha = 0.1f)
                                                     else OrangePrimary.copy(alpha = 0.1f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        // Navigation is now handled reactively via LaunchedEffect(authState)
                    }
                )
            }

            // Admin Dashboard
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    adminViewModel = adminViewModel,
                    onManageProducts = {
                        navController.navigate(Screen.AdminProducts.route)
                    }
                )
            }

            // Admin Products Management
            composable(Screen.AdminProducts.route) {
                AdminProductsScreen(
                    adminViewModel = adminViewModel,
                    onAddProduct = {
                        navController.navigate(Screen.AdminAddProduct.route)
                    },
                    onEditProduct = { productId ->
                        navController.navigate(Screen.AdminEditProduct.createRoute(productId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Admin Add Product
            composable(Screen.AdminAddProduct.route) {
                AdminProductFormScreen(
                    adminViewModel = adminViewModel,
                    productId = null,
                    onBack = { navController.popBackStack() }
                )
            }

            // Admin Edit Product
            composable(
                route = Screen.AdminEditProduct.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                AdminProductFormScreen(
                    adminViewModel = adminViewModel,
                    productId = productId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Menu Screen
            composable(Screen.Menu.route) {
                MenuScreen(
                    modifier = Modifier.fillMaxSize(),
                    onProductClick = { product ->
                        cartViewModel.addToCart(product)
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "${product.name} added to cart",
                                actionLabel = "View Cart",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                navController.navigate(Screen.Cart.route)
                            }
                        }
                    },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    cartItemCount = cartItemCount
                )
            }

            // Cart Screen
            composable(Screen.Cart.route) {
                CartScreen(
                    onCheckoutClick = { address, contactNumber ->
                        val user = currentUser ?: return@CartScreen
                        if (cartState is CartUiState.Success) {
                            val cartItems = (cartState as CartUiState.Success).items
                            if (cartItems.isNotEmpty()) {
                                val orderItems = cartItems.map { OrderItemRequest(it.productId, it.quantity) }
                                val orderRequest = OrderRequest(
                                    userId = user.id,
                                    items = orderItems,
                                    deliveryAddress = address,
                                    contactNumber = contactNumber
                                )
                                orderViewModel.placeOrder(orderRequest) { success, orderId ->
                                    if (success) {
                                        // Only clear cart if order was successful
                                        cartViewModel.clearCart()
                                        scope.launch {
                                            val message = if (orderId != null) "🎉 Order #$orderId placed successfully!" else "🎉 Order placed successfully!"
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        navController.navigate(Screen.Orders.route) {
                                            popUpTo(Screen.Menu.route)
                                        }
                                    } else {
                                        // Show error but keep cart intact
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Failed to place order. Please try again.",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onBackToMenu = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Menu.route) { inclusive = true }
                        }
                    },
                    viewModel = cartViewModel,
                    isPlacingOrder = isPlacingOrder,
                    userMobile = currentUser?.mobile
                )
            }

            // Orders Screen
            composable(Screen.Orders.route) {
                OrderScreen(viewModel = orderViewModel)
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    user = currentUser,
                    onLogout = {
                        authViewModel.logout()
                        // Navigation handled reactively via LaunchedEffect(isLoggedIn)
                    }
                )
            }
        }
    }
}

data class NavigationItem(
    val name: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)
