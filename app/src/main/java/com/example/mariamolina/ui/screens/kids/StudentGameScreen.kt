package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mariamolina.data.model.GamePhase
import com.example.mariamolina.ui.viewmodel.StudentGameViewModel
import kotlinx.coroutines.delay
import kotlin.math.ceil

/**
 * Pantalla del juego para el alumno.
 * Muestra la pregunta actual, opciones para responder y feedback.
 */
@Composable
fun StudentGameScreen(
    pin: String,
    onGameFinished: (String) -> Unit,  // Navega al ranking final con el PIN
    viewModel: StudentGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Iniciar observación al entrar
    LaunchedEffect(pin) {
        viewModel.startObservingGame(pin)
    }

    // Navegar al ranking cuando el juego termine
    LaunchedEffect(uiState.gameFinished) {
        if (uiState.gameFinished) {
            onGameFinished(pin)
        }
    }

    // Temporizador local (lado del cliente)
    val totalTimeMs = 20000L
    var tiempoRestante by remember { mutableStateOf(totalTimeMs) }

    // Resetear temporizador cuando cambia la pregunta
    LaunchedEffect(uiState.partida?.preguntaActualIndex, uiState.gamePhase) {
        if (uiState.gamePhase == GamePhase.SHOWING_QUESTION && !uiState.respuestaEnviada) {
            tiempoRestante = totalTimeMs
            while (tiempoRestante > 0 && !uiState.respuestaEnviada) {
                delay(100L)
                tiempoRestante -= 100L
            }
            // Si el tiempo se acabó y no respondió
            if (tiempoRestante <= 0 && !uiState.respuestaEnviada) {
                viewModel.onTimeUp()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState.gamePhase) {
            GamePhase.LOBBY -> {
                // Esperando a que el profesor inicie
                WaitingScreen(message = "Esperando a que el profesor inicie el juego...")
            }
            
            GamePhase.SHOWING_QUESTION -> {
                // Mostrando pregunta
                QuestionScreen(
                    pregunta = uiState.preguntaActual,
                    opciones = uiState.opcionesAleatorias,
                    tiempoRestante = tiempoRestante,
                    tiempoTotal = totalTimeMs,
                    respuestaEnviada = uiState.respuestaEnviada,
                    respuestaCorrecta = uiState.respuestaCorrecta,
                    puntosObtenidos = uiState.puntosObtenidos,
                    miPuntuacionTotal = uiState.miPuntuacionTotal,
                    preguntaIndex = uiState.partida?.preguntaActualIndex ?: 0,
                    totalPreguntas = uiState.partida?.totalPreguntas ?: 0,
                    onOptionSelected = { index -> viewModel.submitAnswer(index) },
                    mostrarFeedback = uiState.allAnswered || uiState.tiempoAgotado
                )
            }
            
            GamePhase.WAITING_FOR_NEXT -> {
                // Esperando siguiente pregunta
                WaitingForNextScreen(
                    respuestaCorrecta = uiState.respuestaCorrecta,
                    puntosObtenidos = uiState.puntosObtenidos,
                    miPuntuacionTotal = uiState.miPuntuacionTotal,
                    miPosicion = uiState.miPosicion,
                    totalJugadores = uiState.ranking.size
                )
            }
            
            GamePhase.SHOWING_RESULTS -> {
                // Mostrando resultados parciales
                WaitingForNextScreen(
                    respuestaCorrecta = uiState.respuestaCorrecta,
                    puntosObtenidos = uiState.puntosObtenidos,
                    miPuntuacionTotal = uiState.miPuntuacionTotal,
                    miPosicion = uiState.miPosicion,
                    totalJugadores = uiState.ranking.size
                )
            }
            
            GamePhase.FINISHED -> {
                // Juego terminado (se maneja con LaunchedEffect)
                CircularProgressIndicator()
            }
        }

        // Loading overlay
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        // Error
        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error") },
                text = { Text(uiState.error!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun WaitingScreen(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.HourglassEmpty,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(modifier = Modifier.width(200.dp))
    }
}

@Composable
private fun QuestionScreen(
    pregunta: com.example.mariamolina.data.model.QuizQuestion?,
    opciones: List<com.example.mariamolina.data.model.OpcionRespuesta>,
    tiempoRestante: Long,
    tiempoTotal: Long,
    respuestaEnviada: Boolean,
    respuestaCorrecta: Boolean?,
    puntosObtenidos: Int,
    miPuntuacionTotal: Int,
    preguntaIndex: Int,
    totalPreguntas: Int,
    onOptionSelected: (Int) -> Unit,
    mostrarFeedback: Boolean  // Solo mostrar si acertó cuando todos respondan o tiempo agotado
) {
    if (pregunta == null) {
        CircularProgressIndicator()
        return
    }

    val segundos = ceil(tiempoRestante / 1000.0).toInt()
    val progreso = tiempoRestante.toFloat() / tiempoTotal.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Puntuación y progreso
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Puntos: $miPuntuacionTotal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Pregunta ${preguntaIndex + 1}/$totalPreguntas",
                style = MaterialTheme.typography.labelLarge
            )
        }

        LinearProgressIndicator(
            progress = { (preguntaIndex + 1).toFloat() / totalPreguntas.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Timer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            CircularProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp,
                color = if (segundos <= 5) Color.Red else MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$segundos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (segundos <= 5) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pregunta
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = pregunta.pregunta,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Opciones
        opciones.forEachIndexed { index, opcion ->
            // Solo mostrar colores de feedback cuando mostrarFeedback es true
            val showFeedbackColors = respuestaEnviada && mostrarFeedback
            
            val backgroundColor = when {
                !showFeedbackColors -> MaterialTheme.colorScheme.surface
                opcion.esCorrecta -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                !opcion.esCorrecta -> Color(0xFFF44336).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
            
            val borderColor = when {
                !showFeedbackColors -> MaterialTheme.colorScheme.outline
                opcion.esCorrecta -> Color(0xFF4CAF50)
                !opcion.esCorrecta -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.outline
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !respuestaEnviada) { onOptionSelected(index) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = opcion.texto,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Solo mostrar iconos cuando mostrarFeedback es true
                    if (showFeedbackColors) {
                        if (opcion.esCorrecta) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Correcta",
                                tint = Color(0xFF4CAF50)
                            )
                        } else {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Incorrecta",
                                tint = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
        }

        // Feedback después de responder
        if (respuestaEnviada) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (mostrarFeedback) {
                        if (respuestaCorrecta == true) 
                            Color(0xFF4CAF50).copy(alpha = 0.1f) 
                        else 
                            Color(0xFFF44336).copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (mostrarFeedback) {
                        // Mostrar resultado cuando todos han respondido o tiempo agotado
                        Text(
                            text = if (respuestaCorrecta == true) "¡Correcto! 🎉" else "Incorrecto 😔",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (respuestaCorrecta == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        
                        if (puntosObtenidos > 0) {
                            Text(
                                text = "+$puntosObtenidos puntos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Esperando a que todos respondan
                        Text(
                            text = "✓ Respuesta enviada",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Esperando a que todos respondan...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Esperando al profesor...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WaitingForNextScreen(
    respuestaCorrecta: Boolean?,
    puntosObtenidos: Int,
    miPuntuacionTotal: Int,
    miPosicion: Int,
    totalJugadores: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Resultado de la última respuesta
        if (respuestaCorrecta != null) {
            Icon(
                if (respuestaCorrecta) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (respuestaCorrecta) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (respuestaCorrecta) "¡Respuesta correcta!" else "Respuesta incorrecta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (puntosObtenidos > 0) {
                Text(
                    text = "+$puntosObtenidos puntos",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Estadísticas actuales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Tu puntuación",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "$miPuntuacionTotal",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (miPosicion > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Posición: $miPosicion de $totalJugadores",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Esperando siguiente pregunta...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(modifier = Modifier.width(200.dp))
    }
}
