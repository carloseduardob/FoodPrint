package com.example.foodprint.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Dashboard : Routes("dashboard")
    object Inventory : Routes("inventory")
    object Scanner : Routes("scanner")
    object Chef : Routes("chef")
    object Shopping : Routes("shopping")
}