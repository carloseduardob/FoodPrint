package com.example.foodprint.data

import com.example.foodprint.data.local.InventoryDao
import com.example.foodprint.data.local.InventoryItem
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {

    // Expondo os itens como Flow pra UI atualizar sozinha quando o banco mudar.
    val inventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllInventoryItems()
    val shoppingItems: Flow<List<InventoryItem>> = inventoryDao.getAllShoppingItems()

    suspend fun insert(item: InventoryItem) {
        inventoryDao.insertItem(item)
    }

    suspend fun update(item: InventoryItem) {
        inventoryDao.updateItem(item)
    }

    suspend fun delete(item: InventoryItem) {
        inventoryDao.deleteItem(item)
    }
}
