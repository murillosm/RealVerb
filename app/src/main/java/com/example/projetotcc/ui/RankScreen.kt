package com.example.projetotcc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projetotcc.ui.theme.ProjetotccTheme
import com.example.projetotcc.viewmodel.RankUiState
import com.example.projetotcc.viewmodel.RankViewModel
import com.example.projetotcc.viewmodel.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankScreen(
    onBack: () -> Unit,
    viewModel: RankViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
                is RankUiState.Loading -> CircularProgressIndicator()
                is RankUiState.Success -> RankList(users = state.users)
                is RankUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun RankList(users: List<User>) {
    if (users.isEmpty()) {
        Text("No players in the ranking yet.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(users) { index, user ->
            RankItem(rank = index + 1, user = user)
        }
    }
}

@Composable
fun RankItem(rank: Int, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$rank.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = "${user.points} pts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RankScreenPreview() {
    ProjetotccTheme {
        val dummyUsers = listOf(
            User("Jogador 1", 150),
            User("Jogador 2", 125),
            User("Jogador 3", 90)
        )
        RankList(users = dummyUsers)
    }
}