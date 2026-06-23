package com.example.foodprint.data.model

// Modelo simplificado da receita que vem na busca por ingredientes.
data class SpoonacularRecipe(
    val id: Int,
    val title: String, // Título da receita
    val image: String, // URL da foto da comida.
    val usedIngredientCount: Int, // Quantos ingredientes da sua geladeira essa receita usa.
    val missedIngredientCount: Int, // Quantos ingredientes faltam
    val readyInMinutes: Int,
    val servings: Int,
    val measures: Measures?,
    val missedIngredients: List<SpoonacularIngredient> = emptyList() // Lista do que você precisa comprar.
)

// Representação de um passo individual da receita.
data class RecipeStep(
    val number: Int,
    val step: String // O texto explicativo do passo.
)

// Detalhes de cada ingrediente retornado pela API.
data class SpoonacularIngredient(
    val name: String,
    val amount: Double,
    val original: String, // Texto bruto original da API
    val unit: String,
    val measures: Measures?,
    val translatedName: String? // Campo pra tradução feita pelo DeepL.
)

// Diferentes sistemas de medidas
data class Measures(
    val metric: MetricMeasure,
    val us: MetricMeasure
)

// Detalhes da medida
data class MetricMeasure(
    val amount: Double,
    val unitShort: String,
    val unitLong: String
)

// Modelo completo com todas as informações de uma receita específica.
data class RecipeDetail(
    val id: Int,
    val title: String,
    val image: String,
    val readyInMinutes: Int,
    val servings: Int,
    val extendedIngredients: List<SpoonacularIngredient> = emptyList(),
    val analyzedInstructions: List<AnalyzedInstruction> = emptyList()
)

// Agrupa uma lista de passos
data class AnalyzedInstruction(
    val name: String,
    val steps: List<RecipeStep>
)
