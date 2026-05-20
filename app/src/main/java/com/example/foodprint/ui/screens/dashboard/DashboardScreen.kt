package com.example.foodprint.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CardColors
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodprint.data.model.FoodItem
import com.example.foodprint.ui.components.BottomBar
import com.example.foodprint.navigation.Routes
import com.example.foodprint.ui.screens.dashboard.components.ItemRow
import com.example.foodprint.ui.screens.dashboard.components.Section
import com.example.foodprint.ui.screens.dashboard.components.StatCard
import com.example.foodprint.ui.theme.FoodPrintTheme
import androidx.compose.ui.graphics.Color

@Composable
fun DashboardScreen(navController: NavController) {
    val items = listOf(
        FoodItem("Iogurte Natural", 0),
        FoodItem("Alface", 0),
        FoodItem("Presunto", 0),
        FoodItem("Leite", 2),
        FoodItem("Tomate", 3),
        FoodItem("Queijo Mussarela", 4),
        FoodItem("Cenoura", 5),
        FoodItem("Pão de Forma", 6)
    )

    val todayItems = items.filter { it.isExpiringToday }
    val weekItems = items.filter { !it.isExpiringToday }

    val customRedTitle = Color(0xFFBB2622)
    val customRedBackground = Color(0xFFFEF1F1)
    val customYellowTitle = Color(0xFFD18A0A)
    val customYellowBackground = Color(0xFFFEFBE7)

    FoodPrintTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                bottomBar = {
                    BottomBar(navController)
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        navController.navigate(Routes.Scanner.route)
                    }) {
                        Icon(Icons.Default.CameraAlt, null)
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "foodprint",
                        color = Green,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            "Vencendo Hoje",
                            todayItems.count().toString(),
                            color = customRedTitle,
                            Modifier.weight(1f),
                            false,
                            true,
                            customRedBackground
                        )
                        StatCard(
                            "Esta Semana",
                            weekItems.count().toString(),
                            color = customYellowTitle,
                            Modifier.weight(1f),
                            false,
                            true,
                            customYellowBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Section(
                        title = "Vencendo Hoje",
                        items = todayItems,
                        borderColor = customRedTitle,
                        tag = "Hoje",
                        itemBackgroundColor = customRedBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Section(
                        title = "Vencendo Esta Semana",
                        items = weekItems,
                        borderColor = customYellowTitle,
                        itemBackgroundColor = customYellowBackground
                    )
                }
            }
        }
    }
}