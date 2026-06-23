package com.example.foodprint.ui.screens.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodprint.ui.components.BottomBar
import com.example.foodprint.ui.theme.StrongGreen
import com.example.foodprint.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    viewModel: InventoryViewModel
) {
    val itemsList by viewModel.shoppingState.collectAsState()
    var newItemName by remember { mutableStateOf("") }
    var showFinalizationConfirmation by remember { mutableStateOf(false) }

    val pendingCount = itemsList.count { !it.isChecked }
    val completedCount = itemsList.count { it.isChecked }

    val blueHeaderColor = Color(0xFF1A73E8)
    val lightBackgroundColor = Color(0xFFF8F9FA)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = lightBackgroundColor,
        bottomBar = {
            BottomBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Foodprint",
                    color = StrongGreen,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = blueHeaderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🛒 Lista de Compras",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column {
                            Text(text = "Pendentes", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(text = pendingCount.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        Column {
                            Text(text = "Concluídos", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(text = completedCount.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    placeholder = { Text("Adicionar item...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            viewModel.addShoppingItem(newItemName)
                            newItemName = ""
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blueHeaderColor),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = completedCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showFinalizationConfirmation = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StrongGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Concluir ($completedCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.removeCheckedItems() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF0F0),
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Excluir ($completedCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = itemsList, key = { it.id }) { item ->
                    val textColor = if (item.isChecked) Color.Gray.copy(alpha = 0.6f) else Color.Black
                    val textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = item.isChecked,
                                    onClick = {
                                        viewModel.toggleShoppingCheck(item)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF4CAF50)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = item.nome,
                                    fontSize = 16.sp,
                                    color = textColor,
                                    textDecoration = textDecoration
                                )
                            }

                            IconButton(onClick = {
                                viewModel.removeFood(item)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Deletar",
                                    tint = Color(0xFFD32F2F).copy(alpha = if (item.isChecked) 0.4f else 1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFinalizationConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinalizationConfirmation = false },
            title = { Text("Concluir Compra", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja mover os $completedCount itens marcados para o seu inventário?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.finalizeCheckedItems()
                        showFinalizationConfirmation = false
                    }
                ) {
                    Text("Sim, mover", color = StrongGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizationConfirmation = false }) {
                    Text("Não", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}
