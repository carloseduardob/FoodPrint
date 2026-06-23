package com.example.foodprint.ui.screens.chef

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.foodprint.data.model.RecipeDetail
import com.example.foodprint.data.model.SpoonacularRecipe
import com.example.foodprint.ui.viewmodel.ChefUiState
import com.example.foodprint.ui.viewmodel.ChefViewModel
import com.example.foodprint.ui.components.BottomBar
import com.example.foodprint.ui.viewmodel.VoiceAssistantState

// Essa é a tela principal do "Chef".
@Composable
fun ChefScreen(
    navController: NavController,
    viewModel: ChefViewModel = viewModel()
) {
    val searchUiState by viewModel.uiState.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val currentInstructions by viewModel.currentInstructions.collectAsState()
    val recipeDetails by viewModel.recipeDetails.collectAsState()
    
    val context = LocalContext.current

    // Launcher pra pedir permissão de microfone se o usuário ainda não deu.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        }
    }

    // Quando a tela abre, a gente checa a permissão
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            if (voiceState.micStatus != "Ouvindo...") {
                viewModel.toggleListening()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "foodprint chef",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card que mostra se o assistente tá ouvindo ou não.
                AssistantStatusCard(voiceState) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        viewModel.toggleListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Se não tiver nada rolando, mostra as boas-vindas e o botão de carregar receitas.
                if (searchUiState is ChefUiState.Idle) {
                    Text(
                        text = "Crie receitas sem desperdício",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Analisaremos os alimentos do seu inventário para sugerir combinações perfeitas.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.loadRecipes() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("O que posso cozinhar hoje?", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Aqui a gente trata os estados da lista de receitas.
                Box(modifier = Modifier.weight(1f)) {
                    when (val state = searchUiState) {
                        is ChefUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        }
                        is ChefUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                        is ChefUiState.Success -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(state.recipes) { recipe ->
                                    RecipeCard(recipe) { viewModel.loadCookingInstructions(recipe.id) }
                                }
                            }
                        }
                        ChefUiState.Idle -> {}
                    }
                }
            }

            // Se o usuário clicar numa receita, abre o "modal" com os detalhes.
            recipeDetails?.let { details ->
                RecipeDetailsOverlay(
                    recipe = details,
                    instructions = currentInstructions ?: "",
                    voiceState = voiceState,
                    onDismiss = { viewModel.clearInstructions() },
                    onStartChef = { viewModel.startCurrentRecipe() },
                    onFinishChef = { viewModel.finishRecipe() }
                )
            }

            // Se tiver uma receita ativa, a gente cobre a tela com o passo a passo atual.
            if (voiceState.isRecipeActive) {
                CookingOverlay(
                    recipeName = voiceState.activeRecipeName,
                    stepText = voiceState.currentStepText,
                    onShowFullRecipe = {
                        val successState = searchUiState as? ChefUiState.Success
                        val recipeId = successState?.recipes?.find { it.title == voiceState.activeRecipeName }?.id
                        if (recipeId != null) {
                            viewModel.loadCookingInstructions(recipeId)
                        }
                    },
                    onFinish = { viewModel.finishRecipe() }
                )
            }
        }

        // Dialog pra avisar que já tem uma receita ativa se o cara tentar começar outra.
        if (voiceState.showConflictDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelReplacement() },
                title = { Text("Receita em Andamento") },
                text = { Text("Você já tem uma receita ativa (${voiceState.activeRecipeName}). Deseja substituí-la?") },
                confirmButton = {
                    Button(onClick = { viewModel.confirmReplacement() }) {
                        Text("Sim, Substituir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelReplacement() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Cardzinho que mostra se o microfone tá ativo.
@Composable
fun AssistantStatusCard(state: VoiceAssistantState, onMicClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Assistente: ${state.micStatus}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF4A148C)
            )
            IconButton(onClick = onMicClick) {
                Icon(
                    if (state.micStatus == "Ouvindo...") Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = null,
                    tint = if (state.micStatus == "Ouvindo...") Color(0xFF4CAF50) else Color.Gray
                )
            }
        }
    }
}

// Representação de cada receita na lista de busca.
@Composable
fun RecipeCard(recipe: SpoonacularRecipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = recipe.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = recipe.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${recipe.usedIngredientCount} ingredientes usados",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (recipe.missedIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingredientes Faltantes:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                recipe.missedIngredients.forEach { ingredient ->
                    Text(
                        text = "• ${ingredient.amount} ${ingredient.unit} de ${ingredient.name}",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Aproveita os ingredientes do seu inventario",
                color = Color(0xFF4CAF50),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// Overlay que mostra tudo da receita antes de começar.
@Composable
fun RecipeDetailsOverlay(
    recipe: RecipeDetail,
    instructions: String,
    voiceState: VoiceAssistantState,
    onDismiss: () -> Unit,
    onStartChef: () -> Unit,
    onFinishChef: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                AsyncImage(
                    model = recipe.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (recipe.extendedIngredients.isNotEmpty()) {
                    Text(
                        text = "Ingredientes:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recipe.extendedIngredients.forEach { ingredient ->
                        Text(
                            text = "• ${ingredient.original}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧑‍🍳", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Passo a Passo",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = instructions,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botão que alterna entre começar ou finalizar a receita.
                Button(
                    onClick = {
                        if (voiceState.isRecipeActive && voiceState.activeRecipeName == recipe.title) {
                            onFinishChef()
                        } else {
                            onStartChef()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (voiceState.isRecipeActive && voiceState.activeRecipeName == recipe.title) 
                            Color.Red else Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (voiceState.isRecipeActive && voiceState.activeRecipeName == recipe.title) 
                            "🛑 Finalizar Receita" else "👨‍🍳 Começar Receita",
                        fontWeight = FontWeight.Bold
                    )
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Fechar", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Overlay principal enquanto o cara tá cozinhando.
@Composable
fun CookingOverlay(
    recipeName: String,
    stepText: String,
    onShowFullRecipe: () -> Unit,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Cozinhando:", fontSize = 14.sp, color = Color.Gray)
            Text(recipeName, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(48.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (stepText.isEmpty()) "Diga: \"Ei Chefe, passo 1\"" else stepText,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            TextButton(
                onClick = onShowFullRecipe,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Receita Completa", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Finalizar Receita", fontWeight = FontWeight.Bold)
            }
        }
    }
}
