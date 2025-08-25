package com.example.projetotcc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projetotcc.ui.theme.ProjetotccTheme

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onLanguagesClick: () -> Unit,
    onGlossaryClick: () -> Unit,
    onExitClick: () -> Unit,
    onRankClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Main Menu", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLanguagesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Languages")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGlossaryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Glossary")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRankClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rank")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onExitClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exit")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ProjetotccTheme {
        HomeScreen(
            onStartClick = {},
            onLanguagesClick = {},
            onGlossaryClick = {},
            onExitClick = {},
            onRankClick = {}
        )
    }
}
