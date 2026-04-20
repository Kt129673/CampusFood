package com.example.campusfood.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campusfood.ui.screens.MenuScreen
import com.example.campusfood.ui.screens.CartScreen
import com.example.campusfood.ui.screens.CartViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.automirrored.filled.List

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val items = listOf(
        NavigationItem("Menu", Screen.Menu.route, Icons.AutoMirrored.Filled.List),
        NavigationItem("Cart", Screen.Cart.route, Icons.Default.ShoppingCart),
        NavigationItem("Orders", Screen.Orders.route, Icons.AutoMirrored.Filled.List), // Using List for now
        NavigationItem("Profile", Screen.Profile.route, Icons.Default.AccountCircle)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        label = { Text(item.name) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
                        // Optional: Show snackbar or toast
                    },
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    onCheckoutClick = { /* Handle checkout */ },
                    viewModel = cartViewModel
                )
            }
            composable(Screen.Orders.route) {
                PlaceholderScreen("My Orders")
            }
            composable(Screen.Profile.route) {
                PlaceholderScreen("Profile Screen")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Surface {
        Text(text = "Welcome to $name", modifier = Modifier.padding(16.dp))
    }
}

data class NavigationItem(val name: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
