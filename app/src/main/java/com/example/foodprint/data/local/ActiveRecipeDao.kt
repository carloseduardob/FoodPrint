package com.example.foodprint.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActiveRecipeDao {
    // Insere a lista de passos da receita no banco. Se já tiver algo, ele substitui (REPLACE).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(passos: List<PassoReceitaAtiva>)

    // Limpa a tabela toda. Útil quando a gente termina a receita ou desiste dela.
    @Query("DELETE FROM passos_receita_ativa")
    suspend fun deleteAll()

    // Pega um passo específico baseado no número dele. Retorna null se não achar.
    @Query("SELECT * FROM passos_receita_ativa WHERE numeroPasso = :numero")
    suspend fun buscarPasso(numero: Int): PassoReceitaAtiva?

    // Pega o primeiro passo que achar. Serve só pra checar se tem alguma receita ativa rolando.
    @Query("SELECT * FROM passos_receita_ativa LIMIT 1")
    suspend fun obterQualquerPasso(): PassoReceitaAtiva?
}
