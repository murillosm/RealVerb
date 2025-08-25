package com.example.projetotcc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class User(
    val name: String = "",
    val points: Long = 0
)

sealed interface RankUiState {
    object Loading : RankUiState
    data class Success(val users: List<User>) : RankUiState
    data class Error(val message: String) : RankUiState
}

class RankViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _uiState = MutableStateFlow<RankUiState>(RankUiState.Loading)
    val uiState: StateFlow<RankUiState> = _uiState.asStateFlow()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = RankUiState.Loading
            try {
                val snapshot = db.collection("users")
                    .orderBy("points", Query.Direction.DESCENDING)
                    .orderBy("name", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val users = snapshot.toObjects(User::class.java)
                _uiState.value = RankUiState.Success(users)

            } catch (e: Exception) {
                Log.e("RankViewModel", "Erro ao buscar usuários", e)
                _uiState.value = RankUiState.Error("Falha ao carregar o ranking.")
            }
        }
    }
}