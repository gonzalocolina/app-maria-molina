package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.ui.viewmodel.StudentLobbyState
import com.example.mariamolina.ui.viewmodel.StudentLobbyViewModel

@Composable
fun StudentLobbyScreen(
    pin: String,
    onGameStarted: (String) -> Unit, // Callback para ir al juego
    onBack: () -> Unit,
    viewModel: StudentLobbyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Iniciamos la escucha al entrar
    LaunchedEffect(pin) {
        viewModel.escucharPartida(pin)
    }

    // Reaccionamos si el juego empieza
    LaunchedEffect(uiState) {
        if (uiState is StudentLobbyState.GameStarted) {
            val dificultad = (uiState as StudentLobbyState.GameStarted).dificultad
            onGameStarted(dificultad)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = uiState) {
            is StudentLobbyState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Conectando con la sala...")
            }
            is StudentLobbyState.Waiting -> {
                Text("¡Estás dentro!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Esperando a que el profesor inicie el juego...", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                LinearProgressIndicator(modifier = Modifier.width(200.dp)) // Barra de progreso indeterminada
                Spacer(modifier = Modifier.height(32.dp))
                Text("PIN de la sala: $pin", style = MaterialTheme.typography.labelLarge)
            }
            is StudentLobbyState.Error -> {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Salir")
                }
            }
            else -> {} // GameStarted se maneja en el LaunchedEffect
        }
    }
}