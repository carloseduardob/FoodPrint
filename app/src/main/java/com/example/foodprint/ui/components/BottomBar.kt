package com.example.foodprint.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes
import com.example.foodprint.ui.theme.*
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(navController: NavController) {
    val selectedTab = remember { mutableStateOf(1) }

    NavigationBar(
        containerColor = White,
        tonalElevation = 2.dp
    ) {

        NavigationBarItem(
            selected = selectedTab.value == 1,
            onClick = {
                selectedTab.value = 1
                navController.navigate(Routes.Dashboard.route)
                      },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Início") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = selectedTab.value == 2,
            onClick = {
                selectedTab.value = 2
                //navController.navigate(Routes.Inventory.route)
                      },
            icon = { Icon(Icons.Default.Inventory2, null) },
            label = { Text("Inventário") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = selectedTab.value == 3,
            onClick = {
                selectedTab.value = 3
                //navController.navigate(Routes.Scanner.route)
                      },
            icon = { Icon(Icons.Default.CameraAlt, null) },
            label = { Text("Scanner") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = selectedTab.value == 4,
            onClick = {
                selectedTab.value = 4
                //navController.navigate(Routes.Chef.route)
                      },
            icon = { Icon(Icons.Default.Restaurant, null) },
            label = { Text("Chef") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WeekGreen,
                selectedTextColor = WeekGreen
            )
        )

        NavigationBarItem(
            selected = selectedTab.value == 5,
            onClick = {
                selectedTab.value = 5
                //navController.navigate(Routes.Shopping.route)
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