package com.example.foodprint.data.model

data class FoodItem(
    val name: String,
    val daysUntilExpiry: Int
) {
    val isExpiringToday: Boolean
        get() = daysUntilExpiry == 0

    val expiryText: String
        get() = if (isExpiringToday) {
            "Hoje"
        } else {
            "Em $daysUntilExpiry dias"
        }
}