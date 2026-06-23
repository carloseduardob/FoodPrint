package com.example.foodprint.data.remote

import com.example.foodprint.data.model.RecipeDetail
import com.example.foodprint.data.model.SpoonacularRecipe
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Interface pro Retrofit que define as chamadas da API do Spoonacular.
interface RecipeService {
    @GET("recipes/findByIngredients")
    suspend fun findByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 5,
        @Query("apiKey") apiKey: String
    ): List<SpoonacularRecipe>

    // Pega os detalhes de uma receita específica.
    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @Path("id") id: Int,
        @Query("language") language: String = "pt",
        @Query("units") units: String = "metric",
        @Query("apiKey") apiKey: String
    ): RecipeDetail
}