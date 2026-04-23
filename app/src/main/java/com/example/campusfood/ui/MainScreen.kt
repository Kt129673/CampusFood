package com.example.campusfood.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartItemCount = if (cartState is CartUiState.Success) {
        (cartState as CartUiState.Success).items.sumOf { it.quantity }
    } else 0

    val items = listOf(
        NavigationItem(
            name = "Menu",
            route = Screen.Menu.route,
            selectedIcon = Icons.Default.Restaurant,
            unselectedIcon = Icons.Outlined.Restaurant
        ),
        NavigationItem(
            name = "Cart",
            route = Screen.Cart.route,
            selectedIcon = Icons.Default.ShoppingCart,
            unselectedIcon = Icons.Outlined.ShoppingCart,
            badgeCount = cartItemCount
        ),
        NavigationItem(
            name = "Orders",
            route = Screen.Orders.route,
            selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
            unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong
        ),
        NavigationItem(
            name = "Profile",
            route = Screen.Profile.route,
            selectedIcon = Icons.Default.AccountCircle,
            unselectedIcon = Icons.Outlined.AccountCircle
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
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
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                item.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangePrimary,
                            selectedTextColor = OrangePrimary,
                            indicatorColor = OrangePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Menu.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Menu.route) {
                MenuScreen(
                    onProductClick = { product ->
                        cartViewModel.addToCart(product)
                    },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    cartItemCount = cartItemCount
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    onCheckoutClick = {
                        if (cartState is CartUiState.Success) {
                            val cartItems = (cartState as CartUiState.Success).items
                            if (cartItems.isNotEmpty()) {
                                val orderItems = cartItems.map {
                                    OrderItemRequest(it.productId, it.quantity)
                                }
                                val orderRequest = OrderRequest(
                                    userId = 2L, // Rahul Sharma from seed data
                                    items = orderItems,
                                    deliveryAddress = "Campus Dorm A, Room 101"
                                )
                                orderViewModel.placeOrder(orderRequest) {
                                    cartViewModel.clearCart()
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
            composable(Screen.Orders.route) {
                OrderScreen(viewModel = orderViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
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
