package com.example.projetotcc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val action: AuthAction) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

enum class AuthAction {
    LOGIN,
    SIGNUP
}

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Por favor, preencha e-mail e senha.")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authUiState.value = AuthUiState.Success(AuthAction.LOGIN)
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error(e.message ?: "E-mail ou senha inválidos.")
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Por favor, preencha todos os campos.")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid
                if (userId != null) {
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "points" to 0L
                    )
                    db.collection("users").document(userId).set(user).await()
                    _authUiState.value = AuthUiState.Success(AuthAction.SIGNUP)
                } else {
                    _authUiState.value = AuthUiState.Error("Erro ao obter ID do usuário.")
                }
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error(e.message ?: "Ocorreu um erro no cadastro.")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun resetState() {
        _authUiState.value = AuthUiState.Idle
    }
}