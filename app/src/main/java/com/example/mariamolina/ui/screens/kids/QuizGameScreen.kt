package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.EstadoPartida
import com.example.mariamolina.data.model.OpcionRespuesta
import com.example.mariamolina.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun QuizGameScreen(
    dificultad: Dificultad,
    pinPartida: String? = null,
    onQuizFinished: () -> Unit,
    onNavigateToRanking: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val preguntas = uiState.questions
    val puntuacion = uiState.puntuacion
    val indicePreguntaActual = uiState.indicePreguntaActual
    val estadoPartida = uiState.estadoPartida

    LaunchedEffect(key1 = dificultad) {
        if (pinPartida != null) {
            viewModel.conectarAPartida(pinPartida, dificultad)
        } else if (preguntas.isEmpty()) {
            viewModel.loadQuestions(dificultad)
        }
    }

    // --- ESTADO LOCAL ---
    val tiempoTotalPorPregunta = 20000L
    // Estos estados AHORA se reinician SOLO cuando cambia el índice de la pregunta
    var tiempoRestante by remember(indicePreguntaActual) { mutableStateOf(tiempoTotalPorPregunta) }
    var respuestaSeleccionada by remember(indicePreguntaActual) { mutableStateOf<OpcionRespuesta?>(null) }
    var estadoRespuesta by remember(indicePreguntaActual) { mutableStateOf<EstadoRespuesta?>(null) }

    val haRespondidoLocalmente = respuestaSeleccionada != null
    val mostrarResultados = if (pinPartida != null) estadoPartida == EstadoPartida.RESULTADOS else haRespondidoLocalmente

    // --- TIMER ---
    LaunchedEffect(indicePreguntaActual, haRespondidoLocalmente, preguntas.size, estadoPartida) {
        val estamosJugando = if (pinPartida != null) estadoPartida == EstadoPartida.JUGANDO else true

        if (indicePreguntaActual < preguntas.size && !haRespondidoLocalmente && estamosJugando) {
            if (tiempoRestante <= 0) tiempoRestante = tiempoTotalPorPregunta
            while (tiempoRestante > 0 && !haRespondidoLocalmente && (pinPartida == null || estadoPartida == EstadoPartida.JUGANDO)) {
                delay(100L)
                tiempoRestante -= 100L
            }
        }
    }

    // --- LÓGICA DE RESPUESTA ---
    // Este efecto gestiona el envío de puntos y el avance (si es solitario)
    LaunchedEffect(key1 = estadoRespuesta) {
        if (estadoRespuesta != null) {
            // Calculamos y enviamos puntos INMEDIATAMENTE
            val puntosGanados = calcularPuntos(
                respuesta = respuestaSeleccionada,
                tiempoRestante = tiempoRestante,
                tiempoTotal = tiempoTotalPorPregunta
            )
            viewModel.procesarRespuesta(puntosGanados, pinPartida)

            // Si es SOLITARIO, limpiamos para avanzar tras un delay
            if (pinPartida == null) {
                delay(1500)
                respuestaSeleccionada = null
                estadoRespuesta = null
            }
            // ¡IMPORTANTE! Si es MULTIJUGADOR, NO limpiamos nada aquí.
            // Nos quedamos con 'respuestaSeleccionada' != null para mostrar la pantalla de espera.
            // El reseteo ocurrirá automáticamente gracias al 'remember(indicePreguntaActual)'
            // cuando el profesor cambie la pregunta.
        }
    }

    // --- PANTALLAS ---
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${uiState.errorMessage}", color = Color.Red) }
        return
    }

    if ((preguntas.isNotEmpty() && indicePreguntaActual >= preguntas.size) || estadoPartida == EstadoPartida.FINALIZADO) {
        LaunchedEffect(Unit) { if (pinPartida != null) viewModel.guardarPuntuacion(pinPartida) }
        DisposableEffect(Unit) { onDispose { viewModel.resetQuiz() } }
        QuizResultScreen(puntuacion = puntuacion, totalPreguntas = preguntas.size, onVolverAlMenu = onQuizFinished, onNavigateToRanking = onNavigateToRanking)
        return
    }

    if (preguntas.isNotEmpty()) {
        val indiceSeguro = indicePreguntaActual.coerceIn(0, preguntas.lastIndex)
        val preguntaActual = preguntas[indiceSeguro]
        val opcionesAleatorias by remember(preguntaActual) { mutableStateOf(preguntaActual.opciones.shuffled()) }

        // --- PANTALLA DE ESPERA ---
        // Si ya respondiste y el juego sigue en JUGANDO -> Espera
        if (pinPartida != null && haRespondidoLocalmente && !mostrarResultados) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "¡Respuesta enviada!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Esperando a los demás...", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                    LinearProgressIndicator(modifier = Modifier.width(150.dp))
                }
            }
        } else {
            // --- PANTALLA DE JUEGO / RESULTADOS ---
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(id = R.string.kids_quiz_puntuacion, puntuacion), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { (indicePreguntaActual + 1) / preguntas.size.toFloat() }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                        val segundos = ceil(tiempoRestante / 1000.0).toInt()
                        val progreso = tiempoRestante.toFloat() / tiempoTotalPorPregunta.toFloat()
                        CircularProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxSize(), strokeWidth = 8.dp, color = if (segundos <= 5) Color.Red else MaterialTheme.colorScheme.primary)
                        Text("$segundos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = preguntaActual.pregunta, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    opcionesAleatorias.forEach { opcion ->
                        val esCorrectaVisual = if (mostrarResultados) opcion.esCorrecta else false
                        val estadoVisual = if (mostrarResultados) {
                            if (opcion.esCorrecta) EstadoRespuesta.CORRECTA else EstadoRespuesta.INCORRECTA
                        } else null

                        OpcionCard(
                            texto = opcion.texto,
                            seleccionada = respuestaSeleccionada == opcion,
                            estadoRespuesta = if (mostrarResultados || respuestaSeleccionada == opcion) estadoVisual else null,
                            esLaCorrecta = esCorrectaVisual,
                            onClick = {
                                if (!haRespondidoLocalmente) {
                                    respuestaSeleccionada = opcion
                                    estadoRespuesta = if (opcion.esCorrecta) EstadoRespuesta.CORRECTA else EstadoRespuesta.INCORRECTA
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun calcularPuntos(respuesta: OpcionRespuesta?, tiempoRestante: Long, tiempoTotal: Long): Int {
    if (respuesta?.esCorrecta != true) return 0
    val puntosBase = 1000
    val factorTiempo = tiempoRestante.toFloat() / tiempoTotal.toFloat()
    return (puntosBase * factorTiempo).toInt().coerceAtLeast(1)
}

enum class EstadoRespuesta { CORRECTA, INCORRECTA }

@Composable
fun OpcionCard(texto: String, seleccionada: Boolean, estadoRespuesta: EstadoRespuesta?, esLaCorrecta: Boolean, onClick: () -> Unit) {
    val colorBorde = when {
        seleccionada && estadoRespuesta == EstadoRespuesta.CORRECTA -> Color(0xFF4CAF50)
        seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA -> Color(0xFFF44336)
        !seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA && esLaCorrecta -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.outline
    }
    val colorFondo = if (estadoRespuesta != null && seleccionada) colorBorde.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).border(2.dp, colorBorde, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = colorFondo)) {
        Text(text = texto, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge, fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun QuizResultScreen(puntuacion: Int, totalPreguntas: Int, onVolverAlMenu: () -> Unit, onNavigateToRanking: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = stringResource(id = R.string.kids_quiz_finalizado), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "¡Has conseguido $puntuacion puntos!", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onVolverAlMenu, modifier = Modifier.fillMaxWidth()) { Text(stringResource(id = R.string.kids_quiz_volver_menu)) }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onNavigateToRanking, modifier = Modifier.fillMaxWidth()) { Text(stringResource(id = R.string.kids_quiz_ranking)) }
    }
}