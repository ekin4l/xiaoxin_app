package com.seeway.xiaoxinapp.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Voice Assistant Manager
 * Handles voice recognition and AI interactions
 */
class VoiceAssistant(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    companion object {
        private const val TAG = "VoiceAssistant"
        const val VOICE_REQUEST_CODE = 1001
    }

    /**
     * Check if speech recognition is available
     */
    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Start listening for voice input
     */
    suspend fun startListening(): String = suspendCancellableCoroutine { continuation ->
        if (!isSpeechRecognitionAvailable()) {
            continuation.resumeWithException(Exception("Speech recognition not available"))
            return@suspendCancellableCoroutine
        }

        if (isListening) {
            continuation.resumeWithException(Exception("Already listening"))
            return@suspendCancellableCoroutine
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    isListening = true
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Can be used for voice visualization
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    Log.d(TAG, "Buffer received")
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                    isListening = false
                }

                override fun onError(error: Int) {
                    Log.e(TAG, "Speech recognition error: $error")
                    isListening = false
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Unknown error"
                    }
                    if (continuation.isActive) {
                        continuation.resumeWithException(Exception(errorMessage))
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        Log.d(TAG, "Recognition result: ${matches[0]}")
                        if (continuation.isActive) {
                            continuation.resume(matches[0])
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resumeWithException(Exception("No results"))
                        }
                    }
                    isListening = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        Log.d(TAG, "Partial result: ${matches[0]}")
                        // Can be used for real-time feedback
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    Log.d(TAG, "Event: $eventType")
                }
            })

            speechRecognizer?.startListening(intent)

            continuation.invokeOnCancellation {
                stopListening()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }

    /**
     * Destroy speech recognizer
     */
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer", e)
        }
    }

    /**
     * Process voice command and extract intent
     */
    fun processVoiceCommand(text: String): VoiceIntent {
        val lowerText = text.lowercase()

        return when {
            // Navigation intents
            lowerText.contains("导航") || lowerText.contains("去") ||
            lowerText.contains("怎么走") || lowerText.contains("路线") -> {
                val destination = extractDestination(text)
                VoiceIntent.Navigate(destination)
            }

            // Search intents
            lowerText.contains("搜索") || lowerText.contains("找") ||
            lowerText.contains("查") -> {
                val query = extractQuery(text)
                VoiceIntent.Search(query)
            }

            // Weather intent
            lowerText.contains("天气") -> VoiceIntent.CheckWeather

            // Music intent
            lowerText.contains("音乐") || lowerText.contains("播放") -> VoiceIntent.PlayMusic

            // Settings intent
            lowerText.contains("设置") -> VoiceIntent.OpenSettings

            // Help intent
            lowerText.contains("帮助") || lowerText.contains("怎么用") -> VoiceIntent.Help

            // Greeting
            lowerText.contains("你好") || lowerText.contains("嗨") -> VoiceIntent.Greeting

            // Default
            else -> VoiceIntent.General(text)
        }
    }

    private fun extractDestination(text: String): String {
        val patterns = listOf(
            "去(.+)", "导航到(.+)", "到(.+)", "去往(.+)", "前往(.+)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern)
            val match = regex.find(text)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }

        return text
    }

    private fun extractQuery(text: String): String {
        val patterns = listOf(
            "搜索(.+)", "找(.+)", "查(.+)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern)
            val match = regex.find(text)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }

        return text
    }

    /**
     * Voice intent sealed class
     */
    sealed class VoiceIntent {
        data class Navigate(val destination: String) : VoiceIntent()
        data class Search(val query: String) : VoiceIntent()
        data object CheckWeather : VoiceIntent()
        data object PlayMusic : VoiceIntent()
        data object OpenSettings : VoiceIntent()
        data object Help : VoiceIntent()
        data object Greeting : VoiceIntent()
        data class General(val text: String) : VoiceIntent()
    }
}
