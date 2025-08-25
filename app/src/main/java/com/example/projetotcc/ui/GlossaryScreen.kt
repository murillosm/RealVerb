package com.example.projetotcc.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projetotcc.R
import com.example.projetotcc.viewmodel.GlossaryUiState
import com.example.projetotcc.viewmodel.GlossaryViewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.projetotcc.ui.theme.ProjetotccTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    onBack: () -> Unit,
    onWordClick: (String) -> Unit,
    viewModel: GlossaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    GlossaryScreenContent(
        uiState = uiState,
        onBack = onBack,
        onWordClick = onWordClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreenContent(
    uiState: GlossaryUiState,
    onBack: () -> Unit,
    onWordClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Glossary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is GlossaryUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is GlossaryUiState.Success -> {
                    if (state.words.isEmpty()) {
                        Text("No words in the glossary yet.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.words) { word ->
                                val imageName = word.replace("+", " ").lowercase().replace(" ", "_")
                                val context = LocalContext.current
                                val imageId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                                val fallbackId = context.resources.getIdentifier("ic_camera", "drawable", context.packageName)
                                Card(
                                    onClick = { onWordClick(word) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (imageId != 0) {
                                            Image(
                                                painter = painterResource(id = imageId),
                                                contentDescription = word,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.7f)
                                                    .aspectRatio(1f)
                                            )
                                        } else if (fallbackId != 0) {
                                            Image(
                                                painter = painterResource(id = fallbackId),
                                                contentDescription = "Imagem padrão",
                                                modifier = Modifier
                                                    .fillMaxWidth(0.7f)
                                                    .aspectRatio(1f)
                                            )
                                        }
                                        Text(
                                            text = word.replace("+", " "),
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is GlossaryUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Glossary - With Words")
@Composable
fun GlossaryScreenPreview_WithWords() {
    ProjetotccTheme {
        GlossaryScreenContent(
            uiState = GlossaryUiState.Success(listOf("Dog", "Bottle", "Person", "Car")),
            onBack = {},
            onWordClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Glossary - Empty")
@Composable
fun GlossaryScreenPreview_Empty() {
    ProjetotccTheme {
        GlossaryScreenContent(
            uiState = GlossaryUiState.Success(emptyList()),
            onBack = {},
            onWordClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Glossary - Loading")
@Composable
fun GlossaryScreenPreview_Loading() {
    ProjetotccTheme {
        GlossaryScreenContent(
            uiState = GlossaryUiState.Loading,
            onBack = {},
            onWordClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Glossary - Error")
@Composable
fun GlossaryScreenPreview_Error() {
    ProjetotccTheme {
        GlossaryScreenContent(
            uiState = GlossaryUiState.Error("An error occurred while loading the glossary."),
            onBack = {},
            onWordClick = {}
        )
    }
}
