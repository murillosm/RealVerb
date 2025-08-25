package com.example.projetotcc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface GlossaryUiState {
    object Loading : GlossaryUiState
    data class Success(val words: List<String>) : GlossaryUiState
    data class Error(val message: String) : GlossaryUiState
}

class GlossaryViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<GlossaryUiState>(GlossaryUiState.Loading)
    val uiState: StateFlow<GlossaryUiState> = _uiState.asStateFlow()

    init {
        fetchGlossaryWords()
    }

    fun fetchGlossaryWords() {
        viewModelScope.launch {
            _uiState.value = GlossaryUiState.Loading
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.value = GlossaryUiState.Error("Usuário não autenticado.")
                return@launch
            }

            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("glossary")
                    .orderBy("name")
                    .get()
                    .await()

                val words = snapshot.documents.mapNotNull { it.getString("name") }
                _uiState.value = GlossaryUiState.Success(words)
            } catch (e: Exception) {
                Log.e("GlossaryViewModel", "Error fetching glossary", e)
                _uiState.value = GlossaryUiState.Error("Falha ao carregar o glossário.")
            }
        }
    }
}