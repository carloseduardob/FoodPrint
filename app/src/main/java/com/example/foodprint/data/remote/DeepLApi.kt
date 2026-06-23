package com.example.foodprint.data.remote

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

// Data class que mapeia a resposta que vem do DeepL.
data class DeepLResponse(
    val translations: List<DeepLTranslation>
)

// Cada tradução vem dentro de uma lista
data class DeepLTranslation(
    val text: String
)

// Interface que define o endpoint de tradução do DeepL.
interface DeepLApi {
    @POST("v2/translate")
    @FormUrlEncoded
    suspend fun traduzir(
        @Header("Authorization") authKey: String,
        @Field("text") text: String,
        @Field("target_lang") targetLang: String = "PT-BR"
    ): DeepLResponse
}