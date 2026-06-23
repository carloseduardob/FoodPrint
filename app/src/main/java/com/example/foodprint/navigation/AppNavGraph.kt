package com.example.foodprint.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.foodprint.ui.screens.chef.ChefScreen
import com.example.foodprint.ui.screens.dashboard.DashboardScreen
import com.example.foodprint.ui.screens.inventory.InventoryScreen
import com.example.foodprint.ui.screens.scanner.ScannerReviewScreen
import com.example.foodprint.ui.screens.scanner.ScannerScreen
import com.example.foodprint.ui.screens.scanner.ScannerWebViewScreen
import androidx.navigation.NavType
import com.example.foodprint.ui.screens.shopping.ShoppingListScreen
import java.net.URLDecoder

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    
    val inventoryViewModel: com.example.foodprint.ui.viewmodel.InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        val application = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application
        com.example.foodprint.ui.viewmodel.InventoryViewModel(application)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard.route
    ) {
        composable(Routes.Dashboard.route) {
            DashboardScreen(navController, inventoryViewModel)
        }
        composable(Routes.Inventory.route) {
            InventoryScreen(navController, inventoryViewModel)
        }
        composable(Routes.Scanner.route) {
            ScannerScreen(navController, inventoryViewModel)
        }
        composable(Routes.ScannerReview.route) {
            ScannerReviewScreen(navController, inventoryViewModel)
        }
        composable(Routes.Chef.route) {
            ChefScreen(navController)
        }
        composable(
            route = Routes.ScannerWebView.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val urlEncoded = backStackEntry.arguments?.getString("url") ?: ""
            val url = URLDecoder.decode(urlEncoded, "UTF-8")
            ScannerWebViewScreen(url, navController, inventoryViewModel)
        }
        composable(Routes.Shopping.route) {
            ShoppingListScreen(navController, inventoryViewModel)
        }
    }
}
