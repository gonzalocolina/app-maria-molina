package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.ui.viewmodel.JoinGameViewModel
import com.example.mariamolina.ui.viewmodel.JoinGameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGameScreen(
    onBack: () -> Unit,
    onJoinSuccess: (String) -> Unit, // Pasamos el PIN para saber a qué sala ir
    viewModel: JoinGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var nickname by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    // Reaccionar al estado de éxito
    LaunchedEffect(uiState) {
        if (uiState is JoinGameState.Success) {
            onJoinSuccess(pin) // Navegamos a la sala de espera
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) { // Conectamos el botón
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Unirse a Partida",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Tu Nickname (Nombre)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN de la Partida") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is JoinGameState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.joinGame(pin, nickname) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = nickname.isNotBlank() && pin.isNotBlank()
                ) {
                    Text("¡ENTRAR!")
                }
            }

            if (uiState is JoinGameState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (uiState as JoinGameState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}