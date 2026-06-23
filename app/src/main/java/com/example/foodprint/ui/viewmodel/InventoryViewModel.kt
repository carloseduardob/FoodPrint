package com.example.foodprint.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.foodprint.data.InventoryRepository
import com.example.foodprint.data.local.AppDatabase
import com.example.foodprint.data.local.InventoryItem
import com.example.foodprint.util.DateUtils
import com.example.foodprint.util.FoodCategorizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

// ViewModel pra gerenciar o inventário e a lista de compras. Centraliza a lógica de negócio.
class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    val uiState: StateFlow<List<InventoryItem>>
    val shoppingState: StateFlow<List<InventoryItem>>

    // Lista temporária pra revisão após o scanner ler uma nota fiscal ou texto.
    val temporaryList = mutableStateListOf<Pair<String, String>>()
    val isLoading = mutableStateOf(false)
    val debugInfo = mutableStateOf("")

    init {
        // Inicializa o banco e o repository.
        val dao = AppDatabase.getDatabase(application).inventoryDao()
        repository = InventoryRepository(dao)

        // Converte o Flow do banco em StateFlow pra UI consumir com segurança.
        uiState = repository.inventoryItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        shoppingState = repository.shoppingItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Adiciona comida ao inventário. Aceita um nome ou uma lista (separada por \n ou ;).
    fun addFood(name: String, expiryDate: String) {
        viewModelScope.launch {
            val items = if (name.contains("\n")) {
                name.split("\n")
            } else if (name.contains(";")) {
                name.split(";")
            } else {
                listOf(name)
            }

            items.forEach { rawName ->
                val trimmedName = rawName.trim()
                if (trimmedName.isNotBlank()) {
                    // Evita adicionar o mesmo item duas vezes se o nome for igual.
                    val exists = uiState.value.any { it.nome.lowercase() == trimmedName.lowercase() }
                    if (!exists) {
                        val automaticType = FoodCategorizer.getAutomaticCategory(trimmedName)
                        val newItem = InventoryItem(
                            nome = trimmedName,
                            validade = expiryDate,
                            tipo = automaticType
                        )
                        repository.insert(newItem)
                    }
                }
            }
        }
    }

    // Adiciona um item direto na lista de compras.
    fun addShoppingItem(name: String) {
        viewModelScope.launch {
            val newItem = InventoryItem(
                nome = name,
                validade = "",
                tipo = "Compras",
                isInShoppingList = true
            )
            repository.insert(newItem)
        }
    }

    // Remove um alimento permanentemente.
    fun removeFood(item: InventoryItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    // Atualiza o nome e a validade de um item que já existe.
    // A gente também re-categoriza o item se o nome mudar, pra manter a organização.
    fun updateFood(item: InventoryItem, newName: String, newExpiry: String) {
        viewModelScope.launch {
            val automaticType = FoodCategorizer.getAutomaticCategory(newName)
            repository.update(item.copy(
                nome = newName,
                validade = newExpiry,
                tipo = automaticType
            ))
        }
    }

    // Tira do estoque e manda pra lista de compras (útil quando acaba um ingrediente).
    fun moveToShopping(item: InventoryItem) {
        viewModelScope.launch {
            repository.update(item.copy(isInShoppingList = true, isChecked = false))
        }
    }

    // Marca ou desmarca o item na lista de compras.
    fun toggleShoppingCheck(item: InventoryItem) {
        viewModelScope.launch {
            repository.update(item.copy(isChecked = !item.isChecked))
        }
    }

    // Finaliza as compras: o que tá marcado vai pro estoque e ganha uma validade padrão.
    fun finalizeCheckedItems() {
        viewModelScope.launch {
            val itemsToMove = shoppingState.value.filter { it.isChecked }
            itemsToMove.forEach { item ->
                val automaticType = FoodCategorizer.getAutomaticCategory(item.nome)
                repository.update(item.copy(
                    isInShoppingList = false,
                    isChecked = false,
                    validade = DateUtils.generateSimulatedExpiryDate(7),
                    tipo = automaticType
                ))
            }
        }
    }

    // Remove da lista de compras tudo que estiver marcado.
    fun removeCheckedItems() {
        viewModelScope.launch {
            val itemsToRemove = shoppingState.value.filter { it.isChecked }
            itemsToRemove.forEach { repository.delete(it) }
        }
    }

    // Prepara a revisão do scanner. Se for um link (nota fiscal), tenta fazer scraping do site.
    fun prepareReview(rawResult: String) {
        isLoading.value = true
        temporaryList.clear()
        debugInfo.value = ""
        Log.d("FoodPrint", "Starting scan: $rawResult")

        viewModelScope.launch {
            if (rawResult.startsWith("http")) {
                try {
                    val extractedNames = withContext(Dispatchers.IO) {
                        Log.d("FoodPrint", "Connecting to Jsoup...")
                        // Tenta ler a página da nota fiscal pra extrair os nomes dos produtos.
                        val doc = Jsoup.connect(rawResult)
                            .userAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                            .timeout(20000)
                            .followRedirects(true)
                            .get()
                        
                        Log.d("FoodPrint", "Page loaded, searching for elements...")
                        
                        val foundList = mutableListOf<String>()

                        // Vários seletores CSS que as secretarias da fazenda costumam usar.
                        val selectors = listOf(
                            "td.txtTit", "span.txtTit", "#tableItens td.txtTit",
                            "[class*='descricao']", "[id*='descricao']", 
                            ".txtTit", ".descDet", ".txtNome", ".item-name", 
                            ".prod-description"
                        )

                        selectors.forEach { selector ->
                            doc.select(selector).forEach {
                                val text = it.text().trim()
                                // Filtra lixo tipo preços e códigos.
                                if (text.length > 3 && !text.contains("R$") && !text.contains("Vlr") && !text.matches(Regex(".*\\d,\\d\\d.*"))) {
                                    foundList.add(text)
                                }
                            }
                        }

                        // Fallback caso os seletores específicos falhem.
                        if (foundList.isEmpty()) {
                            doc.select("td, span").forEach { element ->
                                val text = element.text().trim()
                                if (text.length > 4 && text == text.uppercase() && !text.contains("CNPJ") && !text.contains("VALOR") && !text.matches(Regex(".*\\d{5,}.*"))) {
                                    foundList.add(text)
                                }
                            }
                        }
                        
                        Log.d("FoodPrint", "Raw items found: ${foundList.size}")
                        foundList
                    }

                    withContext(Dispatchers.Main) {
                        extractedNames.distinct().forEach { name ->
                            if (name.isNotBlank()) {
                                temporaryList.add(name to generateExpiryDate(7))
                            }
                        }
                        isLoading.value = false
                        if (temporaryList.isEmpty()) {
                            debugInfo.value = "Site opened, but could not read names automatically."
                            temporaryList.add("Receipt Link Detected" to generateExpiryDate(0))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FoodPrint", "Scraping error: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        isLoading.value = false
                        debugInfo.value = "Error connecting: ${e.message}"
                        temporaryList.add("Read Error (Receipt Link)" to generateExpiryDate(0))
                    }
                }
            } else {
                // Se não for link, assume que é texto bruto do QR Code.
                isLoading.value = false
                val items = if (rawResult.contains("\n")) {
                    rawResult.split("\n")
                } else if (rawResult.contains(";")) {
                    rawResult.split(";")
                } else {
                    listOf(rawResult)
                }

                items.forEach { item ->
                    val name = item.trim()
                    if (name.isNotEmpty()) {
                        temporaryList.add(name to generateExpiryDate(7))
                    }
                }
            }
        }
    }

    // Salva tudo que foi revisado na lista temporária pro banco definitivo.
    fun completeReview() {
        viewModelScope.launch {
            temporaryList.forEach { (name, expiryDate) ->
                val trimmedName = name.trim()
                val exists = uiState.value.any { it.nome.lowercase() == trimmedName.lowercase() }
                
                if (!exists) {
                    val automaticType = FoodCategorizer.getAutomaticCategory(trimmedName)
                    val newItem = InventoryItem(
                        nome = trimmedName,
                        validade = expiryDate,
                        tipo = automaticType
                    )
                    repository.insert(newItem)
                }
            }
            temporaryList.clear()
        }
    }

    // Helper pra gerar data de validade formatada.
    private fun generateExpiryDate(daysInFuture: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, daysInFuture)
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return formatter.format(calendar.time)
    }
}
