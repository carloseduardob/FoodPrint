package com.example.foodprint.util

import com.example.foodprint.data.remote.DeepLApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Gerenciador de traduções. A gente usa o DeepL porque o Google Translate free é muito limitado.
object TranslationManager {

    // Chave da API (precisa ser a :fx que é a versão free).
    private const val AUTH_KEY = "DeepL-Auth-Key 27c996cd-2034-47a9-90d8-d0184757a8d6:fx"

    // Configuração do Retrofit específica pro endpoint do DeepL.
    private val deepLService: DeepLApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api-free.deepl.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepLApi::class.java)
    }

    // Função que traduz qualquer texto pro nosso português.
    suspend fun translateText(text: String?): String {
        if (text.isNullOrBlank()) return ""

        return try {
            // Manda o texto pro DeepL e pega a primeira tradução da lista.
            val response = deepLService.traduzir(
                authKey = AUTH_KEY,
                text = text,
                targetLang = "PT-BR"
            )
            val rawTranslatedText = response.translations.firstOrNull()?.text ?: text

            // Filtro manual pra corrigir traduções bizarras que o DeepL faz com termos culinários.
            rawTranslatedText
                .replace("bananas com o pedúnculo", "bananas", ignoreCase = true)
                .replace("banana com o pedúnculo", "banana", ignoreCase = true)
                .replace("stem bananas", "bananas", ignoreCase = true)

        } catch (e: Exception) {
            e.printStackTrace()

            // Se a internet cair ou a API der erro, a gente mostra o original em inglês mesmo, melhor do que nada.
            val safeText = text ?: ""
            safeText
                .replace("bananas com o pedúnculo", "bananas", ignoreCase = true)
                .replace("banana com o pedúnculo", "banana", ignoreCase = true)
                .replace("stem bananas", "bananas", ignoreCase = true)
        }
    }
}
