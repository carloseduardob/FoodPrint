package com.example.foodprint.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.foodprint.navigation.Routes

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.Dashboard.route,
            onClick = {
                if (currentRoute != Routes.Dashboard.route) {
                    navController.navigate(Routes.Dashboard.route) {
                        popUpTo(Routes.Dashboard.route) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Inventory.route,
            onClick = {
                if (currentRoute != Routes.Inventory.route) {
                    navController.navigate(Routes.Inventory.route)
                }
            },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Inventário") },
            label = { Text("Inventário") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Scanner.route,
            onClick = {
                if (currentRoute != Routes.Scanner.route) {
                    navController.navigate(Routes.Scanner.route)
                }
            },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner") },
            label = { Text("Scanner") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Chef.route,
            onClick = {
                if (currentRoute != Routes.Chef.route) {
                    navController.navigate(Routes.Chef.route)
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Chef") },
            label = { Text("Chef") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Shopping.route,
            onClick = {
                if (currentRoute != Routes.Shopping.route) {
                    navController.navigate(Routes.Shopping.route)
                }
            },
            icon = {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Compras")
            },
            label = { Text("Compras") }
        )
    }
}
