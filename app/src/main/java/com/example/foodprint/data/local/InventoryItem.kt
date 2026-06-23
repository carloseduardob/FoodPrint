package com.example.foodprint.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val validade: String,
    val tipo: String,
    val isInShoppingList: Boolean = false,
    val isChecked: Boolean = false
)