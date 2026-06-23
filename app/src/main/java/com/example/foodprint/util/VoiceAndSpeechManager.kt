package com.example.foodprint.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

// Essa classe é o coração do assistente de voz. Ela cuida tanto de ouvir (STT) quanto de falar (TTS).
class VoiceAndSpeechManager(
    private val context: Context,
    private val onCommandReceived: (Int) -> Unit,
    private val onNextStepRequested: () -> Unit,
    private val onRepeatRequested: () -> Unit,
    private val onStopRequested: () -> Unit,
    private val onAddInventoryRequested: (String) -> Unit = {},
    private val onRemoveInventoryRequested: (String) -> Unit = {},
    private val onAddShoppingRequested: (String) -> Unit = {},
    private val onStatusChange: (String) -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isListening = false
    private val TAG = "FoodPrintVoice"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var silentAudioTrack: AudioTrack? = null

    init {
        initializeTts()
        initializeSpeechRecognizer()
        prepareSilentAudio()
    }

    // Configura o motor de fala.
    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("pt", "BR")
                isTtsInitialized = true
                setupTtsListener()
            }
        }
    }

    // Callback pra saber quando o app terminou de falar e pode voltar a ouvir o usuário.
    private fun setupTtsListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                stopListeningInternal() // Para de ouvir enquanto fala pra não dar eco.
                onStatusChange("Falando...")
            }

            override fun onDone(utteranceId: String?) {
                startListening() // Volta a ouvir assim que termina de falar.
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                startListening()
            }
        })
    }

    // Configura o reconhecedor de voz nativo do Android.
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    // Hack pra manter o microfone "quente" e evitar aquele barulho de 'beep' chato do Android.
    private fun prepareSilentAudio() {
        val bufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        silentAudioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(8000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        
        val silentBuffer = ShortArray(bufferSize)
        silentAudioTrack?.write(silentBuffer, 0, silentBuffer.size)
        silentAudioTrack?.setLoopPoints(0, bufferSize / 2, -1)
    }

    // Toca ou pausa o áudio silencioso do hack.
    private fun playSilence(play: Boolean) {
        try {
            if (play) {
                silentAudioTrack?.play()
            } else {
                silentAudioTrack?.pause()
                silentAudioTrack?.flush()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silent audio error", e)
        }
    }

    // Remove a função antiga de mute que estava causando o loop
    // Agora o volume é controlado diretamente no start/stop listening

    // Inicia a escuta ativa do microfone.
    fun startListening() {
        if (isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        
        try {
            // Garante que o sistema está mutado ANTES de abrir o microfone
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
            
            playSilence(true)
            speechRecognizer?.startListening(intent)
            isListening = true
            onStatusChange("Ouvindo...")
        } catch (e: Exception) {
            Log.e(TAG, "Start listening error", e)
        }
    }

    // Para de ouvir e limpa o status.
    fun stopListening() {
        stopListeningInternal()
        onStatusChange("Inativo")
        
        // Restaura o volume apenas quando o usuário para o assistente
        try {
            val maxSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
            val maxNotif = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxSystem / 2, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxNotif / 2, 0)
        } catch (e: Exception) {}
    }

    // Helper interno pra parar a escuta sem mudar o status público.
    private fun stopListeningInternal() {
        playSilence(false)
        speechRecognizer?.stopListening()
        isListening = false
    }

    // Manda o celular falar um texto em voz alta.
    fun speak(text: String) {
        if (isTtsInitialized) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "recipe_step")
            }
            // Garante que o canal de música (onde o TTS fala) está desmutado
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "recipe_step")
        }
    }

    // Para a fala atual e volta a ouvir imediatamente.
    fun stopSpeaking() {
        tts?.stop()
        onStatusChange("Ouvindo...")
        startListening()
    }

    // Quando o Google termina de processar o áudio, esse método recebe o texto reconhecido.
    override fun onResults(results: Bundle?) {
        playSilence(false)
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.lowercase() ?: ""
        
        Log.d(TAG, "HEARD: \"$text\"")
        processCommand(text) // Analisa o que o cara disse.
        
        isListening = false
        startListening() // Reabre o microfone pra o próximo comando.
    }

    // Lógica pra identificar palavras-chave e disparar as ações da receita.
    // Agora o comando funciona mesmo sem o "Ei Chefe", pra evitar que o delay do microfone atrapalhe.
    private fun processCommand(text: String) {
        val nextWords = listOf("próximo", "proximo", "seguinte", "continua")
        val repeatWords = listOf("repete", "repetir", "de novo", "denovo", "como é", "não ouvi")
        val stopWords = listOf("parar", "para", "silêncio", "cala a boca", "chega")
        
        // Agora a gente checa os comandos diretamente. Se tiver a wake word, beleza, mas se não tiver
        // e o comando for claro, a gente executa do mesmo jeito pra não perder a fala do usuário.
        when {
            stopWords.any { text.contains(it) } -> onStopRequested()
            nextWords.any { text.contains(it) } -> onNextStepRequested()
            repeatWords.any { text.contains(it) } -> onRepeatRequested()
            
            // Comandos de Inventário e Lista de Compras
            text.contains("adicionar") || text.contains("colocar") || text.contains("tem") || text.contains("tenho") -> {
                val item = extractItemName(text)
                if (item.isNotBlank()) {
                    when {
                        text.contains("lista") || text.contains("compras") || text.contains("carrinho") -> 
                            onAddShoppingRequested(item)
                        else -> onAddInventoryRequested(item)
                    }
                }
            }
            
            text.contains("remover") || text.contains("tirar") || text.contains("acabou") || text.contains("excluir") -> {
                val item = extractItemName(text)
                if (item.isNotBlank()) onRemoveInventoryRequested(item)
            }

            else -> {
                // Se o texto contém apenas um número ou "passo X", a gente tenta buscar.
                val number = extractNumber(text)
                if (number != -1) onCommandReceived(number)
            }
        }
    }

    // Helper para extrair o nome do alimento ignorando palavras de ação e conectivos.
    private fun extractItemName(text: String): String {
        var cleanText = text
        // Remove as Wake Words
        val wakeWords = listOf("chefe", "cheff", "shefe", "ei", "oi")
        wakeWords.forEach { cleanText = cleanText.replace(it, "", ignoreCase = true) }
        
        // Remove palavras de ação e conectivos
        val actions = listOf(
            "adicionar", "colocar", "tem", "tenho", "remover", "tirar", "acabou", "excluir", 
            "no", "na", "o", "a", "à", "ao", "aos", "nas", "nos", "um", "uma"
        )
        actions.forEach { cleanText = cleanText.replace("\\b$it\\b".toRegex(), "") }
        
        // Remove sufixos de destino
        val destinations = listOf("lista de compras", "lista", "carrinho", "inventário", "inventario", "estoque")
        destinations.forEach { cleanText = cleanText.replace(it, "", ignoreCase = true) }
        
        return cleanText.trim().capitalize(Locale.getDefault())
    }

    // Tenta achar um número no meio do texto, seja numeral (1) ou por extenso (um).
    private fun extractNumber(text: String): Int {
        val regex = Regex("(\\d+)")
        val match = regex.find(text)
        if (match != null) return match.value.toInt()
        
        return when {
            text.contains("primeiro") || text.contains("um") || text.contains(" 1") -> 1
            text.contains("segundo") || text.contains("dois") || text.contains(" 2") -> 2
            text.contains("terceiro") || text.contains("três") || text.contains(" 3") -> 3
            text.contains("quarto") || text.contains("quatro") || text.contains(" 4") -> 4
            text.contains("quinto") || text.contains("cinco") || text.contains(" 5") -> 5
            text.contains("sexto") || text.contains("seis") || text.contains(" 6") -> 6
            text.contains("sétimo") || text.contains("sete") || text.contains(" 7") -> 7
            text.contains("oitavo") || text.contains("oito") || text.contains(" 8") -> 8
            text.contains("nono") || text.contains("nove") || text.contains(" 9") -> 9
            text.contains("décimo") || text.contains("dez") || text.contains(" 10") -> 10
            else -> -1
        }
    }

    // Se der erro no reconhecimento, a gente só reseta e volta a ouvir pra não travar a experiência.
    override fun onError(error: Int) {
        isListening = false
        playSilence(false)
        startListening()
    }

    // Métodos obrigatórios da interface que a gente não usa pra essa lógica simples.
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    // Limpa tudo pra evitar memory leaks.
    fun destroy() {
        playSilence(false)
        silentAudioTrack?.release()
        
        // Restaura o volume ao destruir o manager
        try {
            val maxSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
            val maxNotif = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxSystem / 2, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxNotif / 2, 0)
        } catch (e: Exception) {}

        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}
