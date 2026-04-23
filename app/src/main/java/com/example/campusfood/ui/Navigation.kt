package com.example.campusfood.ui

/**
 * Route definitions for the app navigation.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Menu : Screen("menu")
    data object Cart : Screen("cart")
    data object Orders : Screen("orders")
    data object Profile : Screen("profile")
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminProducts : Screen("admin_products")
    data object AdminAddProduct : Screen("admin_add_product")
    data object AdminEditProduct : Screen("admin_edit_product/{productId}") {
        fun createRoute(productId: Long) = "admin_edit_product/$productId"
    }
}
