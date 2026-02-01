package com.seeway.xiaoxinapp.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Text-to-Speech Helper
 * Converts text to speech for AI responses
 */
class SpeechRecognizerHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.CHINA)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Chinese language not supported for TTS")
                // Fallback to English
                textToSpeech?.setLanguage(Locale.ENGLISH)
            } else {
                isInitialized = true
                Log.d(TAG, "TTS initialized successfully")
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
        }
    }

    /**
     * Speak the given text
     */
    fun speak(text: String) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
    }

    /**
     * Speak the given text with parameters
     */
    fun speak(text: String, pitch: Float = 1.0f, speechRate: Float = 1.0f) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }

        textToSpeech?.setPitch(pitch)
        textToSpeech?.setSpeechRate(speechRate)
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
    }

    /**
     * Stop speaking
     */
    fun stop() {
        textToSpeech?.stop()
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }

    /**
     * Destroy TTS
     */
    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
