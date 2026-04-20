package com.example.campusfood.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.campusfood.ui.screens.MenuScreen

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Menu.route) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onProductClick = {
                    // Navigate to details or add to cart
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }
        composable(Screen.Cart.route) {
            // CartScreen()
        }
        composable(Screen.Orders.route) {
            // OrdersScreen()
        }
        composable(Screen.Profile.route) {
            // ProfileScreen()
        }
    }
}
