package es.uva.inf.mariamolina.ui.screens.kids

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import es.uva.inf.mariamolina.data.model.GamePhase
import es.uva.inf.mariamolina.ui.viewmodel.TeacherGameViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla del juego para el profesor.
 * Muestra la pregunta actual, cuántos han respondido, y botón para avanzar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGameScreen(
    pin: String,
    onGameFinished: () -> Unit,
    viewModel: TeacherGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val languageCode = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getString("language", "es") ?: "es"

    // Iniciar observación de la partida al entrar
    LaunchedEffect(pin) {
        viewModel.startObservingGame(pin)
    }

    // Navegar cuando el juego termine
    LaunchedEffect(uiState.gameFinished) {
        if (uiState.gameFinished) {
            onGameFinished()
        }
    }

    // Temporizador para el profesor - sincronizado con el servidor
    val tiempoPorPregunta = uiState.partida?.tiempoPorPregunta ?: 20
    val preguntaIndex = uiState.partida?.preguntaActualIndex ?: 0
    val gamePhase = uiState.partida?.fase
    val preguntaStartedAt = uiState.partida?.preguntaStartedAt
    
    LaunchedEffect(preguntaIndex, gamePhase, preguntaStartedAt) {
        if (gamePhase == GamePhase.SHOWING_QUESTION && preguntaStartedAt != null) {
            val tiempoInicioMs = preguntaStartedAt.toDate().time
            val tiempoPorPreguntaMs = tiempoPorPregunta * 1000L
            
            while (true) {
                val tiempoActualMs = System.currentTimeMillis()
                val tiempoTranscurrido = tiempoActualMs - tiempoInicioMs
                val tiempoRestanteMs = (tiempoPorPreguntaMs - tiempoTranscurrido).coerceAtLeast(0L)
                val tiempoRestanteSegundos = (tiempoRestanteMs / 1000).toInt()
                
                viewModel.updateTiempoRestante(tiempoRestanteSegundos)
                
                if (tiempoRestanteMs <= 0) {
                    // El tiempo ha expirado - esperar 3 segundos de buffer y marcar como expirado
                    delay(3000L)
                    viewModel.marcarTiempoExpirado()
                    break
                }
                
                delay(500L)  // Actualizar cada medio segundo
            }
        } else if (gamePhase == GamePhase.SHOWING_QUESTION && preguntaStartedAt == null) {
            // Fallback: si no hay timestamp (compatibilidad con partidas antiguas)
            var tiempoRestante = tiempoPorPregunta
            viewModel.updateTiempoRestante(tiempoRestante)
            
            while (tiempoRestante > 0) {
                delay(1000L)
                tiempoRestante--
                viewModel.updateTiempoRestante(tiempoRestante)
            }
            
            delay(3000L)
            viewModel.marcarTiempoExpirado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Partida: $pin") 
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.finalizarPartida() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Finalizar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val partida = uiState.partida
            val pregunta = uiState.preguntaActual

            if (partida == null) {
                CircularProgressIndicator()
                return@Scaffold
            }

            // --- PROGRESO ---
            LinearProgressIndicator(
                progress = { 
                    (partida.preguntaActualIndex + 1).toFloat() / partida.totalPreguntas.toFloat() 
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                "Pregunta ${partida.preguntaActualIndex + 1} de ${partida.totalPreguntas}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- PREGUNTA ACTUAL ---
            if (pregunta != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = pregunta.getPregunta(languageCode),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar opciones (solo visualización)
                pregunta.opciones.forEachIndexed { index, opcion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (opcion.esCorrecta) 
                                Color(0xFF4CAF50).copy(alpha = 0.2f) 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${('A' + index)}.",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(30.dp)
                            )
                            Text(text = opcion.getTexto(languageCode))
                            if (opcion.esCorrecta) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Correcta",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TIEMPO RESTANTE ---
            val tiempoRestante = uiState.tiempoRestanteSegundos
            val tiempoExpirado = uiState.tiempoLimiteExpirado
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        tiempoExpirado -> MaterialTheme.colorScheme.errorContainer
                        tiempoRestante <= 5 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        tiempoRestante <= 10 -> Color(0xFFFFF3E0) // Naranja claro
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tiempoExpirado) "⏰ ¡Tiempo agotado!" else "⏱️ Tiempo: ${tiempoRestante}s",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (tiempoExpirado || tiempoRestante <= 5) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- ESTADO DE RESPUESTAS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Respuestas recibidas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "${uiState.jugadoresQueRespondieron} / ${uiState.totalJugadores}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    LinearProgressIndicator(
                        progress = { 
                            if (uiState.totalJugadores > 0) 
                                uiState.jugadoresQueRespondieron.toFloat() / uiState.totalJugadores.toFloat()
                            else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÓN SIGUIENTE PREGUNTA (siempre visible) ---
            val allAnswered = uiState.jugadoresQueRespondieron >= uiState.totalJugadores && 
                              uiState.totalJugadores > 0
            val isLastQuestion = partida.preguntaActualIndex >= partida.totalPreguntas - 1
            
            // El profesor puede avanzar si: todos respondieron O el tiempo expiró
            val puedeAvanzar = allAnswered || tiempoExpirado
            val hayFaltantes = uiState.totalJugadores - uiState.jugadoresQueRespondieron

            Button(
                onClick = { 
                    if (isLastQuestion) {
                        viewModel.finalizarPartida()
                    } else {
                        viewModel.siguientePregunta()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = puedeAvanzar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        allAnswered -> MaterialTheme.colorScheme.primary
                        tiempoExpirado -> Color(0xFFFF9800) // Naranja cuando es por timeout
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = when {
                        allAnswered -> MaterialTheme.colorScheme.onPrimary
                        tiempoExpirado -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Icon(
                    if (isLastQuestion) Icons.Default.EmojiEvents else Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isLastQuestion) "VER RANKING FINAL" else "SIGUIENTE PREGUNTA",
                    fontSize = 16.sp
                )
            }

            // Mostrar estado de respuestas
            Text(
                text = when {
                    allAnswered -> "✓ Todos han respondido - Puedes avanzar"
                    tiempoExpirado && hayFaltantes > 0 -> "⚠️ Tiempo agotado - $hayFaltantes alumno(s) no respondieron"
                    else -> "Esperando: $hayFaltantes de ${uiState.totalJugadores} alumnos"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    allAnswered -> Color(0xFF4CAF50)
                    tiempoExpirado -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- RANKING PARCIAL ---
            if (uiState.ranking.isNotEmpty()) {
                Text(
                    "Ranking actual",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.ranking.take(5).forEachIndexed { index, jugador ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${index + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(30.dp)
                                )
                                if (index < 3) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = when(index) {
                                            0 -> Color(0xFFFFD700)
                                            1 -> Color(0xFFC0C0C0)
                                            else -> Color(0xFFCD7F32)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(jugador.nickname)
                            }
                            Text(
                                "${jugador.puntuacion} pts",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
