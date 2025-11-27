package com.example.mariamolina.ui.screens.kids

import android.content.Context // ¡Importación necesaria!
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // ¡Importación necesaria!
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.OpcionRespuesta
import com.example.mariamolina.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun QuizGameScreen(
    dificultad: Dificultad,
    onQuizFinished: () -> Unit,
    onNavigateToRanking: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    // --- 1. OBTENER IDIOMA SELECCIONADO ---
    val context = LocalContext.current
    val idiomaApp = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("language", "es") ?: "es"
    }
    // --------------------------------------

    val uiState by viewModel.uiState.collectAsState()
    val preguntas = uiState.questions
    val puntuacion = uiState.puntuacion
    val indicePreguntaActual = uiState.indicePreguntaActual

    // Carga inicial
    LaunchedEffect(key1 = dificultad) {
        if (preguntas.isEmpty()) {
            viewModel.loadQuestions(dificultad)
        }
    }

    // --- ESTADO LOCAL ---
    val tiempoTotalPorPregunta = 20000L
    var tiempoRestante by remember(indicePreguntaActual) { mutableStateOf(tiempoTotalPorPregunta) }
    var respuestaSeleccionada by remember(indicePreguntaActual) { mutableStateOf<OpcionRespuesta?>(null) }
    var estadoRespuesta by remember(indicePreguntaActual) { mutableStateOf<EstadoRespuesta?>(null) }

    val haRespondidoLocalmente = respuestaSeleccionada != null
    val mostrarResultados = haRespondidoLocalmente

    // --- TIMER ---
    LaunchedEffect(indicePreguntaActual, haRespondidoLocalmente, preguntas.size) {
        if (indicePreguntaActual < preguntas.size && !haRespondidoLocalmente) {
            if (tiempoRestante <= 0) tiempoRestante = tiempoTotalPorPregunta

            while (tiempoRestante > 0) {
                delay(100L)
                tiempoRestante -= 100L
                // Si el usuario responde, el LaunchedEffect se cancela automáticamente
            }
            // Tiempo agotado sin respuesta
            if (estadoRespuesta == null) {
                estadoRespuesta = EstadoRespuesta.INCORRECTA
            }
        }
    }

    // --- PROCESAR RESPUESTA ---
    LaunchedEffect(estadoRespuesta) {
        if (estadoRespuesta != null) {
            val puntosGanados = calcularPuntos(
                respuesta = respuestaSeleccionada,
                tiempoRestante = tiempoRestante,
                tiempoTotal = tiempoTotalPorPregunta
            )
            viewModel.scoreAndAdvance(puntosGanados)

            delay(1500)
            respuestaSeleccionada = null
            estadoRespuesta = null
        }
    }

    // --- PANTALLAS DE ESTADO ---
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${uiState.errorMessage}", color = Color.Red) }
        return
    }

    // Fin del Juego
    if (preguntas.isNotEmpty() && indicePreguntaActual >= preguntas.size) {
        DisposableEffect(Unit) { onDispose { viewModel.resetQuiz() } }
        QuizResultScreen(puntuacion, onVolverAlMenu = onQuizFinished, onNavigateToRanking = onNavigateToRanking)
        return
    }

    // Pantalla de Juego
    if (preguntas.isNotEmpty()) {
        val indiceSeguro = indicePreguntaActual.coerceIn(0, preguntas.lastIndex)
        val preguntaActual = preguntas[indiceSeguro]
        val opcionesAleatorias by remember(preguntaActual) { mutableStateOf(preguntaActual.opciones.shuffled()) }

        // PANTALLA DE PREGUNTA
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Encabezado
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

                // 2. ¡CORRECCIÓN AQUÍ! Usamos obtenerPregunta(idiomaApp)
                Text(
                    text = preguntaActual.obtenerPregunta(idiomaApp),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Opciones
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                opcionesAleatorias.forEach { opcion ->
                    val esCorrectaVisual = if (mostrarResultados) opcion.esCorrecta else false
                    val estadoVisual = if (mostrarResultados) {
                        if (opcion.esCorrecta) EstadoRespuesta.CORRECTA else EstadoRespuesta.INCORRECTA
                    } else null

                    OpcionCard(
                        // 3. ¡CORRECCIÓN AQUÍ! Usamos obtenerTexto(idiomaApp)
                        texto = opcion.obtenerTexto(idiomaApp),
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

// --- LÓGICA AUXILIAR ---

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
fun QuizResultScreen(puntuacion: Int, onVolverAlMenu: () -> Unit, onNavigateToRanking: () -> Unit) {
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