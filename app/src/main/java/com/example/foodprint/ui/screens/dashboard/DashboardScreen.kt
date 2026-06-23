package com.example.foodprint.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes
import com.example.foodprint.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.example.foodprint.ui.components.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: InventoryViewModel
) {
    val savedItems by viewModel.uiState.collectAsState()

    val backgroundRed = Color(0xFFFFF1F1)
    val textRed = Color(0xFFD32F2F)

    val backgroundYellow = Color(0xFFFFFDE7)
    val textYellow = Color(0xFFF57F17)

    val (itemsToday, itemsThisWeek) = remember(savedItems) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val sevenDaysLater = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, 7)
        }.time

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        val todayList = mutableListOf<com.example.foodprint.data.local.InventoryItem>()
        val weekList = mutableListOf<com.example.foodprint.data.local.InventoryItem>()

        savedItems.forEach { item ->
            try {
                val expiryDate = formatter.parse(item.validade)
                if (expiryDate != null) {
                    if (expiryDate == today) {
                        todayList.add(item)
                    } else if (expiryDate.after(today) && expiryDate.before(sevenDaysLater)) {
                        weekList.add(item)
                    }
                }
            } catch (e: Exception) {
            }
        }
        todayList to weekList
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "foodprint",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            )
        },
        bottomBar = { BottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.Scanner.route) },
                containerColor = Color(0xFF00C853)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = "Escanear Nota Fiscal",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = backgroundRed)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vencendo Hoje", fontSize = 14.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(itemsToday.size.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textRed)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = backgroundYellow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Esta Semana", fontSize = 14.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(itemsThisWeek.size.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textYellow)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (itemsToday.isNotEmpty()) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vencendo Hoje", Modifier.padding(bottom = 8.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        itemsToday.forEach { item ->
                            ItemExpiryLine(item.nome, "Hoje", backgroundColor = backgroundRed, deadlineColor = textRed)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (itemsThisWeek.isNotEmpty()) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vencendo Esta Semana", Modifier.padding(bottom = 8.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        itemsThisWeek.forEach { item ->
                            val deadlineText = calculateDeadlineText(item.validade)
                            ItemExpiryLine(item.nome, deadlineText, backgroundColor = backgroundYellow, deadlineColor = textYellow)
                        }
                    }
                }
            }

            if (itemsToday.isEmpty() && itemsThisWeek.isEmpty()) {
                Spacer(modifier = Modifier.height(40.dp))
                Text("Nenhum item vencendo em breve!", color = Color.Gray, fontSize = 16.sp)
            }
        }
    }
}

fun calculateDeadlineText(expiryDateStr: String): String {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiryDate = formatter.parse(expiryDateStr) ?: return ""
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val differenceMillis = expiryDate.time - currentDate.time
        val days = TimeUnit.MILLISECONDS.toDays(differenceMillis)

        when (days) {
            0L -> "Hoje"
            1L -> "Em 1 dia"
            else -> "Em $days dias"
        }
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun ItemExpiryLine(
    name: String,
    deadline: String,
    backgroundColor: Color,
    deadlineColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
            Text(text = deadline, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = deadlineColor)
        }
    }
}
