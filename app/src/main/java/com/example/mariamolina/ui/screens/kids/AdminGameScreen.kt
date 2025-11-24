package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.data.model.EstadoPartida
import com.example.mariamolina.ui.viewmodel.AdminGameViewModel

@Composable
fun AdminGameScreen(
    pinPartida: String, // Recibimos el PIN para reconectar
    viewModel: AdminGameViewModel = viewModel(),
    onGameFinished: () -> Unit
) {
    // 1. Al entrar, reconectamos con la partida existente
    LaunchedEffect(pinPartida) {
        viewModel.conectarAPartidaExistente(pinPartida)
    }

    val uiState by viewModel.uiState.collectAsState()
    val partida = uiState.partida
    val preguntas = uiState.preguntas // Lista de preguntas reales descargada de Firebase

    // 2. Calculamos la pregunta actual usando la lista real
    val preguntaActual = if (partida != null && preguntas.isNotEmpty() && partida.indicePreguntaActual < preguntas.size) {
        preguntas[partida.indicePreguntaActual]
    } else null

    // 3. Detectar fin del juego
    LaunchedEffect(partida?.estado) {
        if (partida?.estado == EstadoPartida.FINALIZADO) {
            onGameFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pantalla de carga / espera
        if (uiState.isLoading || partida == null || (preguntaActual == null && partida.estado != EstadoPartida.FINALIZADO)) {
            CircularProgressIndicator()
            Text("Sincronizando con la clase...", modifier = Modifier.padding(top = 16.dp))
        } else {

            // --- CABECERA DE ESTADO ---
            Text(
                text = if (partida.estado == EstadoPartida.JUGANDO) "¡TIEMPO EN MARCHA!" else "RESULTADOS",
                style = MaterialTheme.typography.headlineLarge,
                // Color Verde si es Resultados, Azul si está Jugando
                color = if (partida.estado == EstadoPartida.JUGANDO) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contador de preguntas real
            Text(
                text = "Pregunta ${partida.indicePreguntaActual + 1} de ${preguntas.size}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- TARJETA DE PREGUNTA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Texto real de la pregunta desde Firebase
                    Text(
                        text = preguntaActual?.pregunta ?: "Cargando...",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- MONITOR DE RESPUESTAS ---
            val totalJugadores = uiState.jugadoresUnidos.size
            val respondidos = uiState.jugadoresUnidos.count { it.haRespondido }

            Text("Respuestas recibidas:", style = MaterialTheme.typography.titleMedium)

            LinearProgressIndicator(
                progress = if (totalJugadores > 0) respondidos / totalJugadores.toFloat() else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .padding(vertical = 8.dp)
            )

            Text(
                "$respondidos / $totalJugadores",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTONES DE CONTROL ---

            if (partida.estado == EstadoPartida.RESULTADOS) {
                // Fase de Resultados: Botón para ir a la siguiente pregunta
                Text("Mostrando solución a los alumnos...", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.avanzarSiguientePregunta() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("SIGUIENTE PREGUNTA ->", fontSize = 18.sp)
                }
            } else {
                // Fase de Juego: Botón para FORZAR el fin de la pregunta
                Button(
                    onClick = { viewModel.forzarFinPregunta() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("TERMINAR TIEMPO / VER RESULTADOS")
                }
            }
        }

        if (uiState.error != null) {
            Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
        }
    }
}