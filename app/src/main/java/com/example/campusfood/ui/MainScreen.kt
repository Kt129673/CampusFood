package com.example.campusfood.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campusfood.model.OrderItemRequest
import com.example.campusfood.model.OrderRequest
import com.example.campusfood.ui.screens.*
import com.example.campusfood.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isAdmin = currentUser?.role == "ADMIN"

    val cartItemCount = if (cartState is CartUiState.Success) {
        (cartState as CartUiState.Success).items.sumOf { it.quantity }
    } else 0

    // Update OrderViewModel with real userId when user changes
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            orderViewModel.setUserId(user.id)
        }
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

    // Determine start destination
    val startDestination = when {
        !isLoggedIn -> Screen.Login.route
        isAdmin -> Screen.AdminDashboard.route
        else -> Screen.Menu.route
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (isLoggedIn && currentRoute != Screen.Login.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount > 0) {
                                            Badge(containerColor = OrangePrimary, contentColor = Color.White) {
                                                Text("${item.badgeCount}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.name,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(item.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp)
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
                                selectedIconColor = if (isAdmin) Color(0xFF7B1FA2) else OrangePrimary,
                                selectedTextColor = if (isAdmin) Color(0xFF7B1FA2) else OrangePrimary,
                                indicatorColor = if (isAdmin) Color(0xFF7B1FA2).copy(alpha = 0.12f)
                                                 else OrangePrimary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        val dest = if (currentUser?.role == "ADMIN") Screen.AdminDashboard.route
                                   else Screen.Menu.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Admin Dashboard
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen()
            }

            // Menu Screen
            composable(Screen.Menu.route) {
                MenuScreen(
                    onProductClick = { product ->
                        cartViewModel.addToCart(product)
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = "${product.name} added to cart",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    cartItemCount = cartItemCount
                )
            }

            // Cart Screen
            composable(Screen.Cart.route) {
                CartScreen(
                    onCheckoutClick = { address ->
                        val user = currentUser ?: return@CartScreen
                        if (cartState is CartUiState.Success) {
                            val cartItems = (cartState as CartUiState.Success).items
                            if (cartItems.isNotEmpty()) {
                                val orderItems = cartItems.map { OrderItemRequest(it.productId, it.quantity) }
                                val orderRequest = OrderRequest(
                                    userId = user.id,
                                    items = orderItems,
                                    deliveryAddress = address
                                )
                                orderViewModel.placeOrder(orderRequest) {
                                    cartViewModel.clearCart()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "🎉 Order placed successfully!",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    navController.navigate(Screen.Orders.route) {
                                        popUpTo(Screen.Menu.route)
                                    }
                                }
                            }
                        }
                    },
                    viewModel = cartViewModel
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
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
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
