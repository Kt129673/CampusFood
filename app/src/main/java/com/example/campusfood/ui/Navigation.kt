package com.example.campusfood.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusfood.ui.screens.CartScreen
import com.example.campusfood.ui.screens.CartViewModel
import com.example.campusfood.ui.screens.MenuScreen
import com.example.campusfood.ui.screens.OrderScreen
import com.example.campusfood.ui.screens.OrderViewModel
import com.example.campusfood.ui.screens.ProfileScreen

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Menu.route) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onProductClick = { product ->
                    cartViewModel.addToCart(product)
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                }
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(
                viewModel = cartViewModel,
                onCheckoutClick = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Menu.route)
                    }
                }
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
