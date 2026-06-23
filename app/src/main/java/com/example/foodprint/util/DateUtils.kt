package com.example.foodprint.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Objeto utilitário pra centralizar a manipulação de datas no projeto e evitar código repetido.
object DateUtils {
    // Essa função gera uma data de validade simulada. 
    // Ela pega a data atual e soma 'daysInFuture' dias. Útil pra popular o banco em modo dev.
    fun generateSimulatedExpiryDate(daysInFuture: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysInFuture)
        // Formato brasileiro (DD/MM/AAAA) porque a gente é BR, né.
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}
