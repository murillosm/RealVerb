package com.example.projetotcc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PhraseState(
    val original: String,
    var translated: String? = null,
    var isTranslated: Boolean = false
)

sealed interface GlossaryDetailUiState {
    object Loading : GlossaryDetailUiState
    data class Success(val phrases: List<PhraseState>) : GlossaryDetailUiState
    data class Error(val message: String) : GlossaryDetailUiState
}

class GlossaryDetailViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val translator = TranslatorHelper()

    private val _uiState = MutableStateFlow<GlossaryDetailUiState>(GlossaryDetailUiState.Loading)
    val uiState: StateFlow<GlossaryDetailUiState> = _uiState

    fun fetchPhrasesForWord(word: String) {
        viewModelScope.launch {
            _uiState.value = GlossaryDetailUiState.Loading
            val userId = auth.currentUser?.uid
            if (userId == null || word.isBlank()) {
                _uiState.value = GlossaryDetailUiState.Error("Usuário ou palavra inválida.")
                return@launch
            }

            try {
                val document = db.collection("users").document(userId)
                    .collection("glossary").document(word)
                    .get()
                    .await()

                val phrasesData = document.get("phrases")
                val phrases = if (phrasesData is List<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (phrasesData as List<String>).map { PhraseState(original = it) }
                } else {
                    emptyList()
                }
                _uiState.value = GlossaryDetailUiState.Success(phrases)

            } catch (e: Exception) {
                _uiState.value = GlossaryDetailUiState.Error("Falha ao carregar as frases.")
            }
        }
    }

    fun toggleTranslation(phraseIndex: Int) {
        val currentState = _uiState.value
        if (currentState !is GlossaryDetailUiState.Success) return

        val phrases = currentState.phrases.toMutableList()
        val phraseToUpdate = phrases[phraseIndex]

        if (phraseToUpdate.translated != null) {
            phrases[phraseIndex] = phraseToUpdate.copy(isTranslated = !phraseToUpdate.isTranslated)
            _uiState.value = GlossaryDetailUiState.Success(phrases)
        } else {
            translator.translate(
                text = phraseToUpdate.original,
                onSuccess = { translatedText ->
                    phrases[phraseIndex] = phraseToUpdate.copy(
                        translated = translatedText,
                        isTranslated = true
                    )
                    _uiState.value = GlossaryDetailUiState.Success(phrases)
                },
                onError = { error ->
                    Log.e("GlossaryDetailVM", "Erro de tradução: ", error)
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        translator.close()
    }
}