package com.example.projetotcc.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Define o idioma para Inglês
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "O idioma especificado não é suportado.")
            } else {
                isInitialized = true
            }
        } else {
            Log.e("TTS", "Falha ao inicializar o motor de TTS.")
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            Log.e("TTS", "TTS não está inicializado.")
        }
    }

    fun shutdown() {
        if (isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}