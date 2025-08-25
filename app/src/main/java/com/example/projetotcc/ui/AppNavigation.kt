package com.example.projetotcc.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projetotcc.viewmodel.AuthAction
import com.example.projetotcc.viewmodel.AuthUiState
import com.example.projetotcc.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val authState by authViewModel.authUiState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                if (state.action == AuthAction.SIGNUP) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Registration successful! Please log in.")
                    }
                    navController.navigate("login") { popUpTo("signup") { inclusive = true } }
                } else { // Login
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    val startDestination = if (Firebase.auth.currentUser != null) "home" else "login"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginClick = authViewModel::login,
                    onNavigateToRegister = { navController.navigate("signup") },
                    isLoading = authState is AuthUiState.Loading
                )
            }
            composable("signup") {
                SignUpScreen(
                    onRegisterClick = authViewModel::signUp,
                    onNavigateToLogin = { navController.navigate("login") },
                    isLoading = authState is AuthUiState.Loading
                )
            }
            composable("home") {
                HomeScreen(
                    onStartClick = { navController.navigate("category") },
                    onGlossaryClick = { navController.navigate("glossary") },
                    onExitClick = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onLanguagesClick = {},
                    onRankClick = { navController.navigate("rank") }
                )
            }

            composable("category") {
                CategoryScreen(
                    onCategorySelected = { category ->
                        navController.navigate("objectDetection/${category}")
                    }
                )
            }

            composable(
                route = "objectDetection/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                ObjectDetectionScreen(
                    category = category,
                    onBack = { navController.popBackStack() },
                    onObjectDetected = { detectedLabel ->
                        val encodedLabel = URLEncoder.encode(detectedLabel, StandardCharsets.UTF_8.toString())
                        navController.navigate("phraseGeneration/$encodedLabel")
                    }
                )
            }

            composable("rank") {
                RankScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable("glossary") {
                GlossaryScreen(
                    onBack = { navController.popBackStack() },
                    onWordClick = { word ->
                        navController.navigate("glossaryDetail/$word")
                    }
                )
            }


            composable(
                route = "phraseGeneration/{objectLabel}",
                arguments = listOf(navArgument("objectLabel") { type = NavType.StringType })
            ) { backStackEntry ->
                val objectLabel = backStackEntry.arguments?.getString("objectLabel")
                PhraseGenerationScreen(
                    objectLabel = objectLabel,
                    onRecognizeNew = {
                        navController.navigate("category") {
                            popUpTo("home")
                        }
                    },
                    onGoToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "glossaryDetail/{word}",
                arguments = listOf(navArgument("word") { type = NavType.StringType })
            ) { backStackEntry ->
                val word = backStackEntry.arguments?.getString("word")
                if (word != null) {
                    GlossaryDetailScreen(
                        word = word,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}