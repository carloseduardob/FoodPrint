package com.example.foodprint.util

// Classe pra dar uma organizada automática na bagunça do inventário sem o usuário ter que escolher tudo.
object FoodCategorizer {
    // Essa função tenta "adivinhar" a categoria do alimento analisando o nome dele.
    // É um 'when' gigante com várias palavras-chave. Nada de IA complexa aqui, só lógica bruta pra ser rápido.
    fun getAutomaticCategory(foodName: String): String {
        val lowercaseName = foodName.lowercase().trim()

        return when {
            // Lógica pra Laticínios: checa palavras comuns desse grupo.
            lowercaseName.contains("leite") || lowercaseName.contains("queijo") ||
                    lowercaseName.contains("iogurte") || lowercaseName.contains("manteiga") ||
                    lowercaseName.contains("requeijão") || lowercaseName.contains("creme de leite") -> "Laticínios"

            // Lógica pra Proteínas: carnes, ovos e embutidos.
            lowercaseName.contains("frango") || lowercaseName.contains("carne") ||
                    lowercaseName.contains("peixe") || lowercaseName.contains("ovo") ||
                    lowercaseName.contains("bife") || lowercaseName.contains("salsicha") ||
                    lowercaseName.contains("linguiça") || lowercaseName.contains("presunto") -> "Proteínas"

            // Lógica pra Hortifrúti: frutas, legumes e verduras.
            lowercaseName.contains("maçã") || lowercaseName.contains("banana") ||
                    lowercaseName.contains("tomate") || lowercaseName.contains("alface") ||
                    lowercaseName.contains("cebola") || lowercaseName.contains("cenoura") ||
                    lowercaseName.contains("batata") || lowercaseName.contains("limão") ||
                    lowercaseName.contains("fruta") -> "Hortifrúti"

            // Lógica pra Carboidratos & Grãos: coisas que geralmente ficam na dispensa.
            lowercaseName.contains("arroz") || lowercaseName.contains("feijão") ||
                    lowercaseName.contains("macarrão") || lowercaseName.contains("pão") ||
                    lowercaseName.contains("farinha") || lowercaseName.contains("cereal") ||
                    lowercaseName.contains("aveia") -> "Carboidratos & Grãos"

            // Lógica pra Snacks & Doces: guloseimas em geral.
            lowercaseName.contains("biscoito") || lowercaseName.contains("chocolate") ||
                    lowercaseName.contains("refrigerante") || lowercaseName.contains("suco") ||
                    lowercaseName.contains("bala") || lowercaseName.contains("salgadinho") -> "Snacks & Doces"

            // Se o nome não bater com nenhum padrão acima, a gente joga na categoria genérica.
            else -> "Outros"
        }
    }
}
