package com.example.foodprint.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Essa entidade salva os passos de uma receita que o usuário está fazendo no momento.
@Entity(tableName = "passos_receita_ativa")
data class PassoReceitaAtiva(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val receitaId: Int, // ID da receita lá na API do Spoonacular.
    val receitaNome: String,
    val numeroPasso: Int,
    val textoPasso: String
)
