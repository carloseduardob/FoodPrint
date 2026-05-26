package com.example.foodprint.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import com.example.foodprint.ui.screens.dashboard.DashboardScreen
import com.example.foodprint.ui.screens.login.LoginScreen
import com.example.foodprint.ui.screens.inventory.InventoryScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ) {
        composable(Routes.Login.route) {
            LoginScreen(navController)
        }
        composable(Routes.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Routes.Inventory.route) {
            InventoryScreen(navController)
        }
        composable(Routes.Scanner.route) {
            //ScannerScreen()
        }
        composable(Routes.Chef.route) {
            //ChefScreen()
        }
        composable(Routes.Shopping.route) {
            //ShoppingScreen()
        }
    }
}