package com.example.projetotcc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projetotcc.ui.theme.ProjetotccTheme
import com.example.projetotcc.viewmodel.*

@Composable
fun GlossaryDetailScreen(
    word: String,
    onBack: () -> Unit,
    viewModel: GlossaryDetailViewModel = viewModel()
) {
    val context = LocalContext.current

    val isPreview = LocalInspectionMode.current


    val ttsHelper = remember { if (!isPreview) TtsHelper(context) else null }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper?.shutdown()
        }
    }

    LaunchedEffect(word) {
        viewModel.fetchPhrasesForWord(word)
    }

    val uiState by viewModel.uiState.collectAsState()

    GlossaryDetailScreenContent(
        word = word,
        uiState = uiState,
        onBack = onBack,
        onSpeak = { text -> ttsHelper?.speak(text) },
        onTranslate = viewModel::toggleTranslation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryDetailScreenContent(
    word: String,
    uiState: GlossaryDetailUiState,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onTranslate: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Phrases for \"$word\"") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is GlossaryDetailUiState.Loading -> CircularProgressIndicator()
                is GlossaryDetailUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is GlossaryDetailUiState.Success -> {
                    if (state.phrases.isEmpty()) {
                        Text("Nenhuma frase encontrada para esta palavra.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(state.phrases) { index, phraseState ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp)
                                    ) {
                                        // mostra o texto original ou traduzido
                                        val textToShow = if (phraseState.isTranslated) phraseState.translated else phraseState.original
                                        Text(
                                            text = textToShow ?: "Traduzindo...",
                                            modifier = Modifier.align(Alignment.CenterStart).padding(end = 80.dp),
                                            textAlign = TextAlign.Start
                                        )
                                        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                                            // Botão de Áudio
                                            IconButton(onClick = { onSpeak(phraseState.original) }) {
                                                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Ouvir frase")
                                            }
                                            // Botão de Tradução
                                            IconButton(onClick = { onTranslate(index) }) {
                                                Icon(Icons.Default.Translate, "Traduzir")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Tela de Detalhes do Glossário - Sucesso")
@Composable
fun GlossaryDetailScreenPreview() {
    ProjetotccTheme {
        GlossaryDetailScreenContent(
            word = "Dog",
            uiState = GlossaryDetailUiState.Success(
                listOf(
                    PhraseState("This is a friendly dog.", translated = "Este é um cachorro amigável.", isTranslated = false),
                    PhraseState("The dog is playing in the park.", isTranslated = true, translated = "O cachorro está brincando no parque.")
                )
            ),
            onBack = {},
            onSpeak = {},
            onTranslate = {}
        )
    }
}