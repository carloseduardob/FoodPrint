package com.example.foodprint.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes
import com.example.foodprint.ui.theme.*
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = White,
        tonalElevation = 2.dp
    ) {

        NavigationBarItem(
            selected = currentRoute == Routes.Dashboard.route,
            onClick = {
                navController.navigate(Routes.Dashboard.route) {
                    popUpTo(Routes.Dashboard.route)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Início") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Inventory.route,
            onClick = {
                navController.navigate(Routes.Inventory.route) {
                    popUpTo(Routes.Inventory.route)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Inventory2, null) },
            label = { Text("Inventário") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Scanner.route,
            onClick = {
                navController.navigate(Routes.Scanner.route) {
                    popUpTo(Routes.Scanner.route)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.CameraAlt, null) },
            label = { Text("Scanner") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Chef.route,
            onClick = {
                navController.navigate(Routes.Chef.route) {
                    popUpTo(Routes.Chef.route)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Restaurant, null) },
            label = { Text("Chef") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Shopping.route,
            onClick = {
                navController.navigate(Routes.Shopping.route) {
                    popUpTo(Routes.Shopping.route)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            label = { Text("Compras") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )
    }
}