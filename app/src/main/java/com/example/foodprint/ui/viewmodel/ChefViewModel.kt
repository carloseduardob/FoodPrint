package com.example.foodprint.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodprint.data.local.AppDatabase
import com.example.foodprint.data.local.InventoryItem
import com.example.foodprint.data.local.PassoReceitaAtiva
import com.example.foodprint.data.model.RecipeDetail
import com.example.foodprint.data.model.SpoonacularRecipe
import com.example.foodprint.data.remote.RecipeService
import com.example.foodprint.util.DateUtils
import com.example.foodprint.util.FoodCategorizer
import com.example.foodprint.util.TranslationManager
import com.example.foodprint.util.VoiceAndSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Estados da UI pra busca de receitas
sealed interface ChefUiState {
    object Idle : ChefUiState
    object Loading : ChefUiState
    data class Success(val recipes: List<SpoonacularRecipe>) : ChefUiState
    data class Error(val message: String) : ChefUiState
}

// Estado do assistente de voz e da sessão da receita ativa.
data class VoiceAssistantState(
    val isRecipeActive: Boolean = false,
    val activeRecipeName: String = "",
    val currentStepNumber: Int = 0,
    val currentStepText: String = "",
    val micStatus: String = "Inativo",
    val showConflictDialog: Boolean = false,
    val pendingRecipe: List<PassoReceitaAtiva>? = null
)

class ChefViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKey = "a11931c8732844d3b9228840bdc9884a"
    private val db = AppDatabase.getDatabase(application)
    private val inventoryDao = db.inventoryDao()
    private val activeRecipeDao = db.activeRecipeDao()
    
    // Flows que a UI observa pra se manter atualizada
    private val _recipeDetails = MutableStateFlow<RecipeDetail?>(null)
    val recipeDetails = _recipeDetails.asStateFlow()

    // Configuração do Retrofit pra falar com a API do Spoonacular.
    private val apiService = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RecipeService::class.java)

    private val _uiState = MutableStateFlow<ChefUiState>(ChefUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _voiceState = MutableStateFlow(VoiceAssistantState())
    val voiceState = _voiceState.asStateFlow()

    private val _currentInstructions = MutableStateFlow<String?>(null)
    val currentInstructions = _currentInstructions.asStateFlow()

    private var voiceManager: VoiceAndSpeechManager? = null

    init {
        // Quando o ViewModel acorda, já checa se o cara largou alguma receita pela metade no banco.
        checkActiveRecipe()
    }

    // Busca as receitas na API baseada nos ingredientes que o cara tem no estoque.
    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = ChefUiState.Loading
            try {
                // Pega o inventário e transforma numa string zona separada por vírgula.
                val inventory = inventoryDao.getAllInventoryItems().first()
                if (inventory.isEmpty()) {
                    _uiState.value = ChefUiState.Error("Seu inventário está vazio.")
                    return@launch
                }

                val ingredientList = inventory.joinToString(",") { it.nome }
                val recipesRaw = apiService.findByIngredients(ingredientList, 5, apiKey)

                // Traduz as receitas uma por uma usando o DeepL util.
                val translatedRecipes = recipesRaw.map { recipe ->
                    val titlePt = TranslationManager.translateText(recipe.title)

                    val missingIngredientsPt = recipe.missedIngredients.map { ingredient ->
                        ingredient.copy(
                            name = TranslationManager.translateText(ingredient.name),
                            original = TranslationManager.translateText(ingredient.original),
                            unit = TranslationManager.translateText(ingredient.unit)
                        )
                    }

                    recipe.copy(title = titlePt, missedIngredients = missingIngredientsPt)
                }

                _uiState.value = ChefUiState.Success(translatedRecipes)
            } catch (e: Exception) {
                _uiState.value = ChefUiState.Error(e.localizedMessage ?: "Erro desconhecido")
            }
        }
    }

    // Carrega o passo a passo completo da receita escolhida e já manda traduzir tudo de uma vez.
    fun loadCookingInstructions(recipeId: Int) {
        viewModelScope.launch {
            _currentInstructions.value = "Traduzindo..."

            try {
                val details = apiService.getRecipeInformation(
                    id = recipeId,
                    language = "pt",
                    units = "metric",
                    apiKey = apiKey
                )

                // Traduz os ingredientes do detalhe também.
                val translatedIngredients = details.extendedIngredients.map { ingredient ->
                    ingredient.copy(
                        name = TranslationManager.translateText(ingredient.name),
                        original = TranslationManager.translateText(ingredient.original),
                        unit = TranslationManager.translateText(ingredient.unit),
                        translatedName = TranslationManager.translateText(ingredient.name)
                    )
                }

                _recipeDetails.value = details.copy(
                    title = TranslationManager.translateText(details.title),
                    extendedIngredients = translatedIngredients
                )

                val primaryInstruction = details.analyzedInstructions.firstOrNull()

                if (primaryInstruction != null && primaryInstruction.steps.isNotEmpty()) {
                    val stringBuilder = StringBuilder()
                    primaryInstruction.steps.forEach { step ->
                        val translatedStep = TranslationManager.translateText(step.step)
                        stringBuilder.append("${step.number}. $translatedStep\n\n")
                    }
                    _currentInstructions.value = stringBuilder.toString().trim()
                } else {
                    _currentInstructions.value = "Nenhum passo a passo encontrado."
                }

            } catch (e: Exception) {
                _currentInstructions.value = "Falha ao carregar as instruções: ${e.message}"
            }
        }
    }

    // Limpa os estados pra quando fechar o modal ou sair da tela.
    fun clearInstructions() {
        _currentInstructions.value = null
        _recipeDetails.value = null
    }

    // Vê se já tem alguma receita salva no banco local Room pra retomar de onde parou.
    private fun checkActiveRecipe() {
        viewModelScope.launch {
            activeRecipeDao.obterQualquerPasso()?.let { step ->
                _voiceState.value = _voiceState.value.copy(
                    isRecipeActive = true,
                    activeRecipeName = step.receitaNome
                )
            }
        }
    }

    // Liga ou desliga a escuta de voz.
    fun toggleListening() {
        ensureVoiceManagerInitialized()
        if (_voiceState.value.micStatus == "Ouvindo...") {
            voiceManager?.stopListening()
        } else {
            voiceManager?.startListening()
        }
    }

    // Garante que o motor de voz tá instanciado e configurado com os callbacks.
    private fun ensureVoiceManagerInitialized() {
        if (voiceManager == null) {
            voiceManager = VoiceAndSpeechManager(
                context = getApplication(),
                onCommandReceived = { stepNumber -> findStepInDb(stepNumber) },
                onNextStepRequested = { nextStep() },
                onRepeatRequested = { repeatStep() },
                onStopRequested = { stopSpeaking() },
                onAddInventoryRequested = { item -> addInventoryByVoice(item) },
                onRemoveInventoryRequested = { item -> removeInventoryByVoice(item) },
                onAddShoppingRequested = { item -> addShoppingByVoice(item) },
                onStatusChange = { status -> 
                    _voiceState.value = _voiceState.value.copy(micStatus = status)
                }
            )
        }
    }

    // Adiciona item ao inventário via comando de voz.
    private fun addInventoryByVoice(itemName: String) {
        viewModelScope.launch {
            val automaticType = FoodCategorizer.getAutomaticCategory(itemName)
            val newItem = InventoryItem(
                nome = itemName,
                validade = DateUtils.generateSimulatedExpiryDate(7), // Validade padrão de uma semana.
                tipo = automaticType,
                isInShoppingList = false
            )
            inventoryDao.insertItem(newItem)
            voiceManager?.speak("Beleza, adicionei $itemName ao seu inventário.")
        }
    }

    // Remove item do inventário via voz.
    private fun removeInventoryByVoice(itemName: String) {
        viewModelScope.launch {
            val currentItems = inventoryDao.getAllInventoryItems().first()
            val itemToDelete = currentItems.find { it.nome.lowercase() == itemName.lowercase() }
            
            if (itemToDelete != null) {
                inventoryDao.deleteItem(itemToDelete)
                voiceManager?.speak("Pronto, removi $itemName do seu estoque.")
            } else {
                voiceManager?.speak("Não encontrei $itemName no seu inventário para remover.")
            }
        }
    }

    // Adiciona item à lista de compras via voz.
    private fun addShoppingByVoice(itemName: String) {
        viewModelScope.launch {
            val newItem = InventoryItem(
                nome = itemName,
                validade = "",
                tipo = "Compras",
                isInShoppingList = true
            )
            inventoryDao.insertItem(newItem)
            voiceManager?.speak("Ok, coloquei $itemName na sua lista de compras.")
        }
    }

    // Inicia a sessão da receita, salvando os passos no banco pra gente poder acessar por voz mesmo se o cel apagar.
    fun startCurrentRecipe() {
        val details = _recipeDetails.value ?: return
        val primaryInstruction = details.analyzedInstructions.firstOrNull() ?: return

        viewModelScope.launch {
            val stepsToSave = primaryInstruction.steps.map { step ->
                PassoReceitaAtiva(
                    receitaId = details.id,
                    receitaNome = details.title,
                    numeroPasso = step.number,
                    textoPasso = TranslationManager.translateText(step.step)
                )
            }
            
            // Se tentar começar uma receita nova com uma velha ativa, a gente pergunta se quer trocar.
            val existing = activeRecipeDao.obterQualquerPasso()
            if (existing != null && existing.receitaId != details.id) {
                _voiceState.value = _voiceState.value.copy(
                    showConflictDialog = true,
                    pendingRecipe = stepsToSave
                )
            } else {
                saveRecipe(stepsToSave)
            }
        }
    }

    // Limpa a receita velha e salva a nova que tava pendente de confirmação.
    fun confirmReplacement() {
        val pending = _voiceState.value.pendingRecipe ?: return
        viewModelScope.launch {
            activeRecipeDao.deleteAll()
            saveRecipe(pending)
            _voiceState.value = _voiceState.value.copy(showConflictDialog = false, pendingRecipe = null)
        }
    }

    // Descarta a nova e fica com a que já estava rolando.
    fun cancelReplacement() {
        _voiceState.value = _voiceState.value.copy(showConflictDialog = false, pendingRecipe = null)
    }

    // Helper pra gravar a lista de passos no Room de uma vez só.
    private suspend fun saveRecipe(steps: List<PassoReceitaAtiva>) {
        activeRecipeDao.insertAll(steps)
        _voiceState.value = _voiceState.value.copy(
            isRecipeActive = true,
            activeRecipeName = steps.firstOrNull()?.receitaNome ?: "Receita"
        )
    }

    // Deleta tudo do banco local e para de falar. Mata a sessão do "Chef".
    fun finishRecipe() {
        viewModelScope.launch {
            activeRecipeDao.deleteAll()
            _voiceState.value = _voiceState.value.copy(
                isRecipeActive = false,
                activeRecipeName = "",
                currentStepNumber = 0,
                currentStepText = ""
            )
            voiceManager?.stopSpeaking()
            voiceManager?.stopListening()
        }
    }

    // Busca um passo pelo número lá no banco e manda o motor de voz ler o texto pro usuário.
    fun findStepInDb(number: Int) {
        viewModelScope.launch {
            val step = activeRecipeDao.buscarPasso(number)
            if (step != null) {
                _voiceState.value = _voiceState.value.copy(
                    currentStepNumber = number,
                    currentStepText = step.textoPasso
                )
                voiceManager?.speak("Passo $number: ${step.textoPasso}")
            } else {
                voiceManager?.speak("Desculpe, não encontrei o passo $number.")
            }
        }
    }

    // Comando 'próximo passo'.
    fun nextStep() {
        val next = _voiceState.value.currentStepNumber + 1
        findStepInDb(next)
    }

    // Comando 'repete'.
    fun repeatStep() {
        val current = _voiceState.value.currentStepNumber
        if (current > 0) {
            findStepInDb(current)
        } else {
            voiceManager?.speak("Ainda não começamos os passos. Peça o primeiro passo para iniciar.")
        }
    }

    // Manda o TTS parar de falar na hora.
    fun stopSpeaking() {
        voiceManager?.stopSpeaking()
    }

    // Limpa a memória e destroi o motor de voz quando o ViewModel sai de cena.
    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}
