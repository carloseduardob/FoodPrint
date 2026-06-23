package com.example.foodprint.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // Pega tudo que tá no inventário
    @Query("SELECT * FROM inventory_items WHERE isInShoppingList = 0 ORDER BY id DESC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    // Pega só os itens que o usuário marcou pra lista de compras.
    @Query("SELECT * FROM inventory_items WHERE isInShoppingList = 1 ORDER BY id DESC")
    fun getAllShoppingItems(): Flow<List<InventoryItem>>

    // Adiciona um item novo. Se o ID já existir, ele sobrescreve pra evitar duplicata.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    // Atualiza as infos de um item que já tá lá.
    @Update
    suspend fun updateItem(item: InventoryItem)

    // Deleta o item de vez do banco.
    @Delete
    suspend fun deleteItem(item: InventoryItem)
}
