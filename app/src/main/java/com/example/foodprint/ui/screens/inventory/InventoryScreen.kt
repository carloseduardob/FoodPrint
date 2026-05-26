package com.example.foodprint.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodprint.ui.components.BottomBar
import com.example.foodprint.ui.theme.FoodPrintTheme
import com.example.foodprint.ui.theme.*
import com.example.foodprint.ui.screens.inventory.components.ToggleHeader
import com.example.foodprint.ui.screens.inventory.components.InventoryItem

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

                    ToggleHeader()

                    Spacer(modifier = Modifier.height(16.dp))

                    // 📦 List
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    }

                    InventoryItem(
                        name = "Iogurte Natural",
                        category = "Laticínios",
                        date = "Validade: 28/04/2026",
                        tag = "HOJE",
                        color = Red
                    )

                    InventoryItem(
                        name = "Alface",
                        category = "Vegetais",
                        date = "Validade: 28/04/2026",
                        tag = "HOJE",
                        color = Red
                    )

                    InventoryItem(
                        name = "Presunto",
                        category = "Carnes",
                        date = "Validade: 28/04/2026",
                        tag = "HOJE",
                        color = Red
                    )

                    InventoryItem(
                        name = "Leite",
                        category = "Laticínios",
                        date = "Validade: 30/04/2026",
                        tag = "2d",
                        color = Yellow
                    )
                }
            }
        }
    }
}