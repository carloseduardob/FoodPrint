package com.example.foodprint.ui.screens.scanner

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodprint.navigation.Routes
import com.example.foodprint.ui.viewmodel.InventoryViewModel
import com.example.foodprint.util.DateUtils
import org.json.JSONArray

// Essa tela abre o site da SEFAZ numa WebView pra gente conseguir extrair os itens da nota fiscal.
// É um "hack" necessário porque muitas notas precisam de captcha ou interação do usuário.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerWebViewScreen(
    url: String,
    navController: NavController,
    viewModel: InventoryViewModel
) {
    var isExtracting by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validação da Nota") },
                actions = {
                    if (isExtracting) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp), strokeWidth = 2.dp)
                    } else {
                        // Botão manual caso a extração automática demore muito ou falhe.
                        Button(
                            onClick = { 
                                webViewRef?.evaluateJavascript("window.extrair()", null)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Capturar Itens", fontSize = 12.sp)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        settings.javaScriptEnabled = true // Precisamos de JS pra injetar o script de extração.
                        settings.domStorageEnabled = true
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                // Injeta um script JS na página pra varrer o DOM em busca de nomes de produtos.
                                view?.evaluateJavascript("""
                                    window.extrair = function() {
                                        var items = [];
                                        
                                        var extractFromDoc = function(doc) {
                                            var found = [];
                                            var selectors = [
                                                'td.txtTit', 
                                                'span.txtTit', 
                                                '#tableItens td.txtTit',
                                                '.fixo-prod-serv-descricao span',
                                                'td.descricao',
                                                '.txtNme',
                                                'span[id^="lblNme"]'
                                            ];
                                            
                                            selectors.forEach(function(s) {
                                                var elements = doc.querySelectorAll(s);
                                                elements.forEach(function(el) {
                                                    var text = el.innerText.trim();
                                                    // Filtra textos que não parecem ser nomes de produtos (preços, códigos, etc).
                                                    if (text.length > 3 && 
                                                        !text.includes('R$') && 
                                                        !text.includes('VALOR') &&
                                                        !text.match(/^\d+$/) && 
                                                        !text.match(/.*\d,\d\d.*/)) {
                                                        if (!found.includes(text)) found.push(text);
                                                    }
                                                });
                                            });
                                            return found;
                                        };

                                        // Busca no documento principal.
                                        items = extractFromDoc(document);

                                        // Busca recursiva em todos os IFrames (comum em portais SEFAZ que usam arquitetura antiga).
                                        var iframes = document.querySelectorAll('iframe');
                                        iframes.forEach(function(iframe) {
                                            try {
                                                var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                                                var iframeItems = extractFromDoc(iframeDoc);
                                                iframeItems.forEach(function(it) {
                                                    if (!items.includes(it)) items.push(it);
                                                });
                                            } catch(e) {
                                                console.log("Erro ao acessar iframe: ", e);
                                            }
                                        });

                                        // Fallback genérico se os seletores específicos não funcionarem.
                                        if (items.length === 0) {
                                            var cells = document.querySelectorAll('table tr td');
                                            cells.forEach(function(td) {
                                                var text = td.innerText.trim();
                                                if (text.length > 5 && text === text.toUpperCase() && !text.contains('CNPJ') && !text.contains('TOTAL')) {
                                                    if (!items.includes(text)) items.push(text);
                                                }
                                            });
                                        }
                                        
                                        // Se achou algo, manda pro app Android via ponte JS.
                                        if (items.length > 0) {
                                            AndroidBridge.postItems(JSON.stringify(items));
                                        } else {
                                            // Tenta de novo em 2 segundos se não achou nada (pode ser delay de carregamento).
                                            setTimeout(window.extrair, 2000);
                                        }
                                    };
                                    // Primeira tentativa automática após 4 segundos.
                                    setTimeout(window.extrair, 4000);
                                """.trimIndent(), null)
                            }
                        }

                        // Ponte que recebe os dados do JavaScript e joga no ViewModel.
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun postItems(json: String) {
                                val array = JSONArray(json)
                                if (array.length() > 0) {
                                    post {
                                        isExtracting = true
                                        viewModel.temporaryList.clear()
                                        for (i in 0 until array.length()) {
                                            val name = array.getString(i)
                                            viewModel.temporaryList.add(name to DateUtils.generateSimulatedExpiryDate(7))
                                        }
                                        // Vai pra tela de revisão pra o usuário confirmar o que foi lido.
                                        navController.navigate(Routes.ScannerReview.route) {
                                            popUpTo(Routes.Scanner.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }, "AndroidBridge")

                        loadUrl(url)
                    }
                }
            )
            
            if (!isExtracting) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp
                ) {
                    Text(
                        "Aguardando a nota carregar... Valide o captcha se solicitado.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
