@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.example.foodprint.ui.screens.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes
import com.example.foodprint.ui.viewmodel.InventoryViewModel

// Tela pra revisar o que o scanner leu. O usuário pode apagar o que a "IA" ou o scraper leu errado.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerReviewScreen(
    navController: NavController,
    viewModel: InventoryViewModel
) {
    val primaryGreen = Color(0xFF2E7D32)
    val itemsToReview = viewModel.temporaryList // Itens que ainda não foram salvos no banco.
    val isLoading by viewModel.isLoading
    val debugInfo by viewModel.debugInfo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisar Itens", color = primaryGreen, fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Enquanto o scraper do Jsoup tá rodando, mostra o spinner.
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = primaryGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Lendo nota fiscal...", color = Color.Gray)
                }
            } else if (itemsToReview.isEmpty() && debugInfo.isNotEmpty()) {
                // Se deu ruim na leitura, avisa o usuário.
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(debugInfo, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)) {
                        Text("Voltar")
                    }
                }
            } else {
                // Lista de itens pra confirmar.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${itemsToReview.size} itens encontrados",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(itemsToReview) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.first, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Validade: ${item.second}", fontSize = 12.sp, color = Color.Gray)
                                    }

                                    // Se o scanner ler algo nada a ver (tipo "TOTAL" ou "FORMA PAGTO"), o usuário deleta aqui.
                                    IconButton(
                                        onClick = { itemsToReview.removeAt(index) }
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Remover",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Salva tudo de vez no banco local Room.
                    Button(
                        onClick = {
                            viewModel.completeReview()
                            navController.navigate(Routes.Inventory.route) {
                                popUpTo(Routes.Dashboard.route)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = itemsToReview.isNotEmpty()
                    ) {
                        Text("Concluir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (itemsToReview.isEmpty()) {
                        TextButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
