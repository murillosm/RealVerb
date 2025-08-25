package com.example.projetotcc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetotcc.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlin.math.pow

class GeminiViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Initial)
    val uiState: StateFlow<GeminiUiState> = _uiState

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.API_KEY
    )

    fun generatePhrases(objectName: String) {
        _uiState.value = GeminiUiState.Loading
        val cleanObjectName = objectName.split(",").firstOrNull()?.trim() ?: objectName
        val prompt = "Crie DUAS frases curtas e simples em inglês sobre: $cleanObjectName. Separe as frases com '|||'. lembre o mouse não é mouse animal e o mouse identificado e mouse de computador."

        viewModelScope.launch {
            var success = false
            var lastError: Exception? = null

            for (attempt in 1..3) {
                try {
                    val response = generativeModel.generateContent(prompt)
                    response.text?.let { text ->
                        val phrases = text.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
                        if (phrases.isNotEmpty()) {
                            _uiState.value = GeminiUiState.Success(phrases)
                            saveWordToGlossary(cleanObjectName, phrases)
                            success = true
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    lastError = e
                    Log.w("GeminiViewModel", "Tentativa $attempt falhou: ${e.message}")
                    delay(1000L * 2.0.pow(attempt - 1).toLong())
                }
            }

            if (!success) {
                val errorMessage = lastError?.localizedMessage ?: "Ocorreu um erro desconhecido."
                _uiState.value = if (errorMessage.contains("overloaded")) {
                    GeminiUiState.Error("Serviço indisponível. Tente novamente mais tarde.")
                } else {
                    GeminiUiState.Error(errorMessage)
                }
            }
        }
    }

    // Adiciona a lógica para salvar a palavra no Firestore
    // A função lê antes de escrever para evitar duplicatas
    private suspend fun saveWordToGlossary(word: String, newPhrases: List<String>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("Firestore", "Usuário não autenticado, não foi possível salvar a palavra.")
            return
        }

        val capitalizedWord = word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        if (capitalizedWord.isBlank()) return

        try {
            val docRef = db.collection("users").document(userId)
                .collection("glossary").document(capitalizedWord)

            val document = docRef.get().await()

            val existingPhrases = if (document.exists()) {
                val phrasesData = document.get("phrases")
                if (phrasesData is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    phrasesData as List<String>
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }

            val allPhrases = (existingPhrases + newPhrases).distinct()

            val wordData = hashMapOf(
                "name" to capitalizedWord,
                "phrases" to allPhrases,
                "timestamp" to FieldValue.serverTimestamp()
            )

            docRef.set(wordData).await()
            Log.d("Firestore", "Dados do glossário para '$capitalizedWord' atualizados com sucesso.")

        } catch (e: Exception) {
            Log.w("Firestore", "Erro ao atualizar dados no glossário", e)
        }
    }
}

sealed interface GeminiUiState {
    object Initial : GeminiUiState
    object Loading : GeminiUiState
    data class Success(val phrases: List<String>) : GeminiUiState
    data class Error(val message: String) : GeminiUiState
}