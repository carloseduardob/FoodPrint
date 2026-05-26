package com.example.foodprint.ui.screens.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.unit.dp

@Composable
fun InventoryItem(
    name: String,
    category: String,
    date: String,
    tag: String,
    color: Color
) {
    Box {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, color, RoundedCornerShape(16.dp))
        ) {

            Row(
                modifier = Modifier
                    .background(LightGray)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(category, color = Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(date, color = Gray)
                }

                Text(
                    text = tag,
                    color = color,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // 🔴 little corner accent (top-left)
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
    }
}