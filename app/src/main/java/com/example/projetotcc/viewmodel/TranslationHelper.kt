package com.example.projetotcc.viewmodel

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class TranslatorHelper {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.PORTUGUESE)
        .build()

    private val translator: Translator = Translation.getClient(options)
    private var isModelDownloaded = false

    init {
        downloadModelIfNeeded()
    }

    private fun downloadModelIfNeeded() {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                isModelDownloaded = true
                Log.d("TranslatorHelper", "Modelo de tradução (EN-PT) baixado com sucesso.")
            }
            .addOnFailureListener { exception ->
                isModelDownloaded = false
                Log.e("TranslatorHelper", "Falha ao baixar modelo de tradução.", exception)
            }
    }

    fun translate(
        text: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!isModelDownloaded) {
            downloadModelIfNeeded()
            onError(Exception("Modelo de tradução não está pronto. Tente novamente em alguns segundos."))
            return
        }

        translator.translate(text)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onError)
    }

    // Libera os recursos do tradutor
    fun close() {
        translator.close()
    }
}