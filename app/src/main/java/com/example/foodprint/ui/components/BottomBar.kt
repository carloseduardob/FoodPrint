package com.example.foodprint.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes

@Composable
fun BottomBar(navController: NavController) {
    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Início") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Inventory.route) },
            icon = { Icon(Icons.Default.Inventory2, null) },
            label = { Text("Inventário") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Scanner.route) },
            icon = { Icon(Icons.Default.CameraAlt, null) },
            label = { Text("Scanner") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Chef.route) },
            icon = { Icon(Icons.Default.Restaurant, null) },
            label = { Text("Chef") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Shopping.route) },
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            label = { Text("Compras") }
        )
    }
}