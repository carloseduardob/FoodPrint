package com.example.foodprint.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [InventoryItem::class, PassoReceitaAtiva::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // DAOs que a gente usa pra mexer no banco
    abstract fun inventoryDao(): InventoryDao
    abstract fun activeRecipeDao(): ActiveRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Padrão Singleton pra garantir que a gente não abra várias conexões com o banco ao mesmo tempo.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodprint_database"
                )
                .fallbackToDestructiveMigration() // Se a gente mudar o esquema, ele deleta e cria de novo
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}