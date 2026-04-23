package com.example.campusfood.ui

/**
 * Route definitions for the app navigation.
 */
sealed class Screen(val route: String) {
    data object Menu : Screen("menu")
    data object Cart : Screen("cart")
    data object Orders : Screen("orders")
    data object Profile : Screen("profile")
}
