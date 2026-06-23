@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.example.foodprint.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes
import com.example.foodprint.ui.viewmodel.InventoryViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import com.example.foodprint.ui.components.BottomBar

// Tela do scanner. Aqui a gente usa a CameraX e o ML Kit pra ler QR Codes de notas fiscais.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val primaryGreen = Color(0xFF2E7D32)
    var isNoteLoading by remember { mutableStateOf(false) }
    var rawDataDebug by remember { mutableStateOf("") } // Pra gente ver o que tá sendo lido durante o desenvolvimento.

    // Checagem de permissão de câmera. Sem isso, nada feito.
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    // Aquela linha verde que fica subindo e descendo pra dar um efeito de scanner profissional.
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // Pede a permissão assim que a tela abre, se necessário.
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("foodprint", color = primaryGreen, fontWeight = FontWeight.Bold, fontSize = 24.sp) }
            )
        },
        bottomBar = { BottomBar(navController) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF111111)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(if (isNoteLoading) "Processando Nota Fiscal..." else "Abastecendo a despensa", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (isNoteLoading) "Aguarde, estamos extraindo seus alimentos da SEFAZ" else "Aproxime a câmera do código para listar os produtos", color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasPermission && !isNoteLoading) {
                        // Renderiza o preview da câmera e aciona o callback quando acha um QR Code.
                        CameraPreviewScanner(
                            modifier = Modifier.fillMaxSize(),
                            onQrCodeScanned = { result ->
                                rawDataDebug = result
                                if (result.startsWith("http")) {
                                    // Se for link, vai pra WebView pra confirmar o acesso.
                                    navController.navigate(Routes.ScannerWebView.createRoute(result))
                                } else {
                                    // Se for texto puro, vai direto pra revisão.
                                    viewModel.prepareReview(result)
                                    navController.navigate(Routes.ScannerReview.route)
                                }
                            }
                        )
                        // Desenha a linha animada por cima da câmera.
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val yCoord = size.height * animatedLineY
                            drawLine(color = primaryGreen, start = Offset(0f, yCoord), end = Offset(size.width, yCoord), strokeWidth = 3.dp.toPx())
                        }
                    } else if (isNoteLoading) {
                        CircularProgressIndicator(color = primaryGreen)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.DarkGray, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aguardando permissão de câmera...", color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp)
                        }
                    }

                    Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(16.dp), color = Color.Transparent, border = BorderStroke(4.dp, primaryGreen)) {}
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Card de instrução pro usuário não se perder.
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Dica de Mestre 💡", color = Color(0xFFFFB300), fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        Text("Mire diretamente no QR Code quadrado impresso na sua nota fiscal. Nós faremos a leitura automática de todos os alimentos de uma só vez para você!", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    }
                }

                // Área de debug pra ajudar a gente a ver o que a câmera tá pegando de verdade.
                if (rawDataDebug.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("LOG DE SCAN (DEBUG)", color = Color(0xFFFFEB3B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
                            Box(modifier = Modifier.height(60.dp).verticalScroll(rememberScrollState())) {
                                Text(rawDataDebug, color = Color.White, fontSize = 11.sp, lineHeight = 14.sp)
                            }
                            Button(
                                onClick = { 
                                    clipboardManager.setText(AnnotatedString(rawDataDebug))
                                    Toast.makeText(context, "Copiado!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.align(Alignment.End).height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                            ) {
                                Text("Copiar", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componente que integra a CameraX com o ML Kit pra processar os frames da câmera em tempo real.
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewScanner(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var isScanned by remember { mutableStateOf(false) } // Flag pra evitar múltiplos disparos do mesmo código.

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val executor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Configura a análise de imagem pra rodar o scanner em cada frame.
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val scanner = BarcodeScanning.getClient()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && !isScanned) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue
                                    if (rawValue != null) {
                                        isScanned = true
                                        onQrCodeScanned(rawValue)
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close() // Importante fechar o proxy pra não vazar memória.
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}
