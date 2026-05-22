package com.example.foodprint.ui.screens.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier,
    boldTitle : Boolean = false,
    boldText : Boolean = false,
    backgroundColor : Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color),
        colors = if (backgroundColor == null) CardDefaults.cardColors() else CardColors(backgroundColor,backgroundColor,backgroundColor, backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title, color = color, fontWeight = if (boldTitle) FontWeight.Bold else null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(count, color = color, style = MaterialTheme.typography.headlineMedium, fontWeight = if (boldText) FontWeight.Bold else null)
        }
    }
}