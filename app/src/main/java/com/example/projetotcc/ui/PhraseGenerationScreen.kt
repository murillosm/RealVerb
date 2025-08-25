package com.example.projetotcc.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projetotcc.ui.theme.ProjetotccTheme
import com.example.projetotcc.viewmodel.GeminiUiState
import com.example.projetotcc.viewmodel.GeminiViewModel
import com.example.projetotcc.viewmodel.TtsHelper
import com.example.projetotcc.viewmodel.TranslatorHelper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class TranslatablePhrase(
    val original: String,
    var translated: String? = null,
    var isTranslated: Boolean = false
)

@Composable
fun PhraseGenerationScreen(
    objectLabel: String?,
    onRecognizeNew: () -> Unit, // 1. Nova ação de clique
    onGoToHome: () -> Unit,     // 2. Nova ação de clique
    viewModel: GeminiViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val ttsHelper = remember { if (!isPreview) TtsHelper(context) else null }
    val translatorHelper = remember { if (!isPreview) TranslatorHelper() else null }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper?.shutdown()
            translatorHelper?.close()
        }
    }

    val decodedLabel = remember(objectLabel) {
        try {
            URLDecoder.decode(objectLabel ?: "", StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            objectLabel ?: ""
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var phrases by remember { mutableStateOf<List<TranslatablePhrase>>(emptyList()) }

    LaunchedEffect(uiState) {
        if (uiState is GeminiUiState.Success) {
            phrases = (uiState as GeminiUiState.Success).phrases.map { TranslatablePhrase(original = it) }
        }
    }

    LaunchedEffect(decodedLabel) {
        if (decodedLabel.isNotBlank()) {
            viewModel.generatePhrases(decodedLabel)
        }
    }

    PhraseGenerationScreenContent(
        uiState = uiState,
        objectName = decodedLabel.split(",").firstOrNull() ?: "Desconhecido",
        phrases = phrases,
        onRecognizeNew = onRecognizeNew,
        onGoToHome = onGoToHome,
        onSpeak = { text -> ttsHelper?.speak(text) },
        onTranslate = { index ->
            val phrase = phrases[index]
            if (phrase.translated != null) {
                phrases = phrases.mapIndexed { i, p ->
                    if (i == index) p.copy(isTranslated = !p.isTranslated) else p
                }
            } else {
                translatorHelper?.translate(
                    text = phrase.original,
                    onSuccess = { translatedText ->
                        phrases = phrases.mapIndexed { i, p ->
                            if (i == index) p.copy(translated = translatedText, isTranslated = true) else p
                        }
                    },
                    onError = { error ->
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseGenerationScreenContent(
    uiState: GeminiUiState,
    objectName: String,
    phrases: List<TranslatablePhrase>,
    onRecognizeNew: () -> Unit,
    onGoToHome: () -> Unit,
    onSpeak: (String) -> Unit,
    onTranslate: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generated Phrases") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Object: $objectName",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is GeminiUiState.Loading, GeminiUiState.Initial -> {
                        CircularProgressIndicator()
                        Text("Generating phrases...", modifier = Modifier.padding(top = 16.dp))
                    }
                    is GeminiUiState.Success -> {
                        PhrasesList(
                            phrases = phrases,
                            onSpeak = onSpeak,
                            onTranslate = onTranslate
                        )
                    }
                    is GeminiUiState.Error -> {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onRecognizeNew,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recognize New")
                }
                Button(
                    onClick = onGoToHome,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
private fun PhrasesList(
    phrases: List<TranslatablePhrase>,
    onSpeak: (String) -> Unit,
    onTranslate: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        phrases.forEachIndexed { index, phrase ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val textToShow = if (phrase.isTranslated) phrase.translated else phrase.original
                    Text(
                        text = textToShow ?: "Translating...",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 48.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { onSpeak(phrase.original) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen to phrase",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Botão de Tradução (direita)
                        IconButton(onClick = { onTranslate(index) }) {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = "Traduzir frase",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
        if (phrases.size < 2) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, name = "PhraseGenerationScreenPreview")
@Composable
fun PhraseGenerationScreenPreview_Success() {
    ProjetotccTheme {
        PhraseGenerationScreenContent(
            uiState = GeminiUiState.Success(listOf("This is a friendly dog.", "The dog is playing.")),
            objectName = "Dog",
            phrases = listOf(
                TranslatablePhrase("This is a friendly dog."),
                TranslatablePhrase("The dog is playing.")
            ),
            onSpeak = {},
            onTranslate = {},
            onRecognizeNew = {},
            onGoToHome = {}
        )
    }
}