package com.example.foodprint.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodprint.ui.components.BottomBar
import com.example.foodprint.ui.theme.FoodPrintTheme
import com.example.foodprint.ui.theme.*

@Composable
fun InventoryScreen(navController: NavController) {
    FoodPrintTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                bottomBar = {
                    BottomBar(navController)
                },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Foodprint",
                        color = StrongGreen,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}