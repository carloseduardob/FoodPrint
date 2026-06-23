package com.example.foodprint.ui.screens.inventory

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes
import androidx.compose.ui.platform.LocalContext
import com.example.foodprint.data.local.InventoryItem
import com.example.foodprint.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.example.foodprint.ui.components.BottomBar

// Tela principal do inventário. Aqui a gente lista o que tem em casa e permite adicionar coisas novas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // Controla se tá vendo Geladeira ou Armário
    var showDialog by remember { mutableStateOf(false) } // Dialog de opções do item
    var showManualDialog by remember { mutableStateOf(false) } // Dialog pra add na mão
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") } // Campo de busca

    // Estados temporários pra edição do item
    var editName by remember { mutableStateOf("") }
    var editExpiry by remember { mutableStateOf("") }

    val savedItems by viewModel.uiState.collectAsState()

    val primaryGreen = Color(0xFF2E7D32)
    val textRed = Color(0xFFD32F2F)

    // Filtra os itens da geladeira e aplica a busca do usuário.
    val fridgeItems = remember(savedItems, searchQuery) {
        savedItems
            .filter { (it.tipo == "Laticínios" || it.tipo == "Proteínas" || it.tipo == "Hortifrúti") &&
                    it.nome.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.nome.lowercase() }
    }
    // Filtra os itens do armário e aplica a busca.
    val pantryItems = remember(savedItems, searchQuery) {
        savedItems
            .filter { (it.tipo == "Carboidratos & Grãos" || it.tipo == "Snacks & Doces" || it.tipo == "Outros") &&
                    it.nome.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.nome.lowercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("foodprint", color = primaryGreen, fontWeight = FontWeight.Bold, fontSize = 24.sp) }
            )
        },
        bottomBar = { BottomBar(navController) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botão pra digitar o nome do alimento.
                FloatingActionButton(
                    onClick = { showManualDialog = true },
                    containerColor = primaryGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Manual")
                }
                // Botão pra abrir a câmera e escanear (mais rápido).
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.Scanner.route) },
                    containerColor = Color(0xFF00C853)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Campo de busca bonitão com ícone de lupa.
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Pesquisar item...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryGreen) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // Tabs pra separar geladeira de armário
            TabRow(
                selectedTabIndex = selectedTab,
                contentColor = primaryGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = primaryGreen
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Geladeira", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Armário", fontWeight = FontWeight.Bold) })
            }

            // Lista dinâmica baseada na tab selecionada.
            if (selectedTab == 0) {
                if (fridgeItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Geladeira vazia", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(fridgeItems, key = { it.id }) { item ->
                            DynamicInventoryCard(item = item) {
                                selectedItem = item
                                editName = item.nome
                                editExpiry = item.validade
                                showDialog = true
                            }
                        }
                    }
                }
            } else {
                if (pantryItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Armário vazio", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pantryItems, key = { it.id }) { item ->
                            DynamicInventoryCard(item = item) {
                                selectedItem = item
                                editName = item.nome
                                editExpiry = item.validade
                                showDialog = true
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog que aparece quando clica em adicionar manual.
    if (showManualDialog) {
        ManualAddDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { name ->
                val simulatedExpiry = generateManualExpiryDate(30)
                viewModel.addFood(name, simulatedExpiry)
                Toast.makeText(context, "✅ $name adicionado!", Toast.LENGTH_SHORT).show()
                showManualDialog = false
            }
        )
    }

    // Dialog de opções quando clica num item da lista.
    if (showDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Editar Item", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Altere os dados do alimento abaixo:", fontSize = 14.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen)
                    )
                    
                    OutlinedTextField(
                        value = editExpiry,
                        onValueChange = { editExpiry = it },
                        label = { Text("Validade (DD/MM/AAAA)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateFood(selectedItem!!, editName, editExpiry)
                        Toast.makeText(context, "✅ Item atualizado!", Toast.LENGTH_SHORT).show()
                        showDialog = false
                    }
                ) {
                    Text("Salvar", color = primaryGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.moveToShopping(selectedItem!!)
                            Toast.makeText(context, "✅ Movido para compras!", Toast.LENGTH_SHORT).show()
                            showDialog = false
                        }
                    ) {
                        Text("Mover Compras", color = Color.Gray)
                    }
                    TextButton (
                        onClick = {
                            viewModel.removeFood(selectedItem!!)
                            Toast.makeText(context, "Item excluído!", Toast.LENGTH_SHORT).show()
                            showDialog = false
                        }
                    ) {
                        Text("Excluir", color = textRed)
                    }
                }
            }
        )
    }
}

// Card que representa cada item. Ele muda de cor se estiver perto de vencer.
@Composable
fun DynamicInventoryCard(item: InventoryItem, onClick: () -> Unit) {
    val expiryData = remember(item.validade) { calculateExpiryData(item.validade) }
    val (backgroundColor, textColor, tagText) = expiryData

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.tipo, fontSize = 12.sp, color = Color.Gray)
                Text("Validade: ${item.validade}", fontSize = 12.sp, color = Color.LightGray)
            }

            // Tag visual pra indicar urgência.
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                color = backgroundColor
            ) {
                Text(
                    text = tagText,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Lógica pra calcular quantos dias faltam e qual cor o card deve ter.
fun calculateExpiryData(expiryDateStr: String): Triple<Color, Color, String> {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiryDate = formatter.parse(expiryDateStr) ?: return Triple(Color(0xFFF5F5F5), Color.Gray, "---")
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val differenceMillis = expiryDate.time - currentDate.time
        val days = TimeUnit.MILLISECONDS.toDays(differenceMillis)

        when {
            days < 0 -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "VENCIDO")
            days == 0L -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "HOJE")
            days <= 2 -> Triple(Color(0xFFFFFDE7), Color(0xFFF57F17), "${days}d")
            else -> Triple(Color(0xFFF5F5F5), Color.Gray, "${days}d")
        }
    } catch (e: Exception) {
        Triple(Color(0xFFF5F5F5), Color.Gray, "Erro")
    }
}

// Modal pra adicionar alimentos digitando.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var foodName by remember { mutableStateOf("") }
    val primaryGreen = Color(0xFF2E7D32)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novos Alimentos", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Adicione um item por linha ou separe por ponto e vírgula (;)", 
                    fontSize = 14.sp, 
                    color = Color.Gray, 
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Nomes dos Alimentos") },
                    placeholder = { Text("Ex: Leite\nArroz\nFeijão") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        focusedLabelColor = primaryGreen
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (foodName.isNotBlank()) onConfirm(foodName) },
                enabled = foodName.isNotBlank()
            ) {
                Text("Adicionar Todos", color = primaryGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

// Gera uma data futura formatada pra ser usada no banco de dados.
fun generateManualExpiryDate(daysInFuture: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysInFuture)
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(calendar.time)
}
