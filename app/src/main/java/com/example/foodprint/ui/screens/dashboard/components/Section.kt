package com.example.foodprint.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.foodprint.data.model.FoodItem

@Composable
fun Section(
    title: String,
    items: List<FoodItem>,
    borderColor: Color,
    tag: String? = null,
    itemBackgroundColor : Color = Color(0xFFF3F4F6)
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            items.forEach { item ->
                ItemRow(item, borderColor, itemBackgroundColor)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}