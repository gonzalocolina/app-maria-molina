package com.example.mariamolina.ui.screens.kids

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Importante para obtener el ViewModel
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.OpcionRespuesta
import com.example.mariamolina.ui.viewmodel.QuizViewModel // Importante
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun QuizGameScreen(
    pinPartida: String? = null,
    dificultad: Dificultad,
    onQuizFinished: () -> Unit,
    onNavigateToRanking: () -> Unit,
    // Obtenemos el ViewModel. Si usas Hilt sería hiltViewModel(), si no, viewModel() está bien.
    viewModel: QuizViewModel = viewModel()
) {
    // 1. Observamos el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val preguntas = uiState.questions
    val puntuacion = uiState.puntuacion
    val indicePreguntaActual = uiState.indicePreguntaActual

    // 2. Carga inicial de preguntas desde Firebase
    LaunchedEffect(key1 = dificultad) {
        // Solo cargamos si la lista está vacía para evitar recargas al rotar pantalla
        if (preguntas.isEmpty()) {
            viewModel.loadQuestions(dificultad)
        }
    }

    // --- ESTADO LOCAL (Temporizador y selección) ---
    val tiempoTotalPorPregunta = 20000L
    // Usamos 'remember(indicePreguntaActual)' para reiniciar el timer en cada pregunta nueva
    var tiempoRestante by remember(indicePreguntaActual) { mutableStateOf(tiempoTotalPorPregunta) }
    var respuestaSeleccionada by remember { mutableStateOf<OpcionRespuesta?>(null) }
    var estadoRespuesta by remember { mutableStateOf<EstadoRespuesta?>(null) }


    // --- LÓGICA DEL TEMPORIZADOR ---
    LaunchedEffect(key1 = indicePreguntaActual, key2 = estadoRespuesta, key3 = preguntas.size) {
        // Solo corre el tiempo si hay preguntas cargadas y no se ha respondido aún
        if (indicePreguntaActual < preguntas.size && estadoRespuesta == null) {
            tiempoRestante = tiempoTotalPorPregunta
            while (tiempoRestante > 0 && estadoRespuesta == null) {
                delay(100L)
                tiempoRestante -= 100L
            }
            // Si el tiempo llega a 0 y no se respondió -> INCORRECTA
            if (estadoRespuesta == null && tiempoRestante <= 0) {
                estadoRespuesta = EstadoRespuesta.INCORRECTA
            }
        }
    }

    // --- AVANCE AUTOMÁTICO ---
    // Cuando estadoRespuesta cambia (se responde o se acaba el tiempo), esperamos y avanzamos
    LaunchedEffect(key1 = estadoRespuesta) {
        if (estadoRespuesta != null) {
            delay(1500) // Pausa para ver el resultado (verde/rojo)

            // Calculamos puntos
            val puntosGanados = calcularPuntos(
                respuesta = respuestaSeleccionada,
                tiempoRestante = tiempoRestante,
                tiempoTotal = tiempoTotalPorPregunta
            )

            // Llamamos al ViewModel para actualizar puntuación y pasar de pregunta
            viewModel.procesarRespuesta(puntosGanados, pinPartida)

            // Reseteamos estado local para la siguiente
            respuestaSeleccionada = null
            estadoRespuesta = null
        }
    }


    // --- GESTIÓN DE PANTALLAS (Carga, Error, Juego, Fin) ---

    // 1. Pantalla de Carga
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // 2. Pantalla de Error
    if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Error: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            // Podrías añadir un botón de "Reintentar" aquí llamando a viewModel.loadQuestions(dificultad)
        }
        return
    }

    // 3. Pantalla de Fin de Juego
    if (preguntas.isNotEmpty() && indicePreguntaActual >= preguntas.size) {
        // Limpiamos el quiz al salir (efecto de un solo uso)
        DisposableEffect(Unit) {
            onDispose { viewModel.resetQuiz() }
        }

        QuizResultScreen(
            puntuacion = puntuacion,
            totalPreguntas = preguntas.size,
            onVolverAlMenu = onQuizFinished,
            onNavigateToRanking = onNavigateToRanking
        )
        return
    }

    // 4. Pantalla de Juego (Si hay preguntas y no hemos terminado)
    if (preguntas.isNotEmpty()) {
        val preguntaActual = preguntas[indicePreguntaActual]

        // Opciones mezcladas (lo hacemos una vez por pregunta)
        val opcionesAleatorias by remember(preguntaActual) {
            mutableStateOf(preguntaActual.opciones.shuffled())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Scroll para pantallas pequeñas
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- ENCABEZADO (Puntos, Barra, Reloj, Pregunta) ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.kids_quiz_puntuacion, puntuacion),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (indicePreguntaActual + 1) / preguntas.size.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Widget Reloj
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    val segundos = ceil(tiempoRestante / 1000.0).toInt()
                    val progreso = tiempoRestante.toFloat() / tiempoTotalPorPregunta.toFloat()

                    CircularProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = if (segundos <= 5) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$segundos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pregunta (Desde Firebase es String)
                Text(
                    text = preguntaActual.pregunta,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // --- OPCIONES DE RESPUESTA ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                opcionesAleatorias.forEach { opcion ->
                    OpcionCard(
                        texto = opcion.texto, // Desde Firebase es String
                        seleccionada = respuestaSeleccionada == opcion,
                        estadoRespuesta = if (respuestaSeleccionada == opcion) estadoRespuesta else null,
                        esLaCorrecta = opcion.esCorrecta,
                        onClick = {
                            // Solo permitimos responder si no hay estado (no respondido ni tiempo agotado)
                            if (estadoRespuesta == null) {
                                respuestaSeleccionada = opcion
                                estadoRespuesta = if (opcion.esCorrecta) EstadoRespuesta.CORRECTA else EstadoRespuesta.INCORRECTA
                                // El LaunchedEffect se encarga de calcular puntos y avanzar
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

// Función pura para calcular puntos
private fun calcularPuntos(respuesta: OpcionRespuesta?, tiempoRestante: Long, tiempoTotal: Long): Int {
    // Si no respondió o respondió mal -> 0 puntos
    if (respuesta?.esCorrecta != true) {
        return 0
    }

    // Puntuación Kahoot: Base + Bonus por velocidad
    val puntosBase = 1000
    val factorTiempo = tiempoRestante.toFloat() / tiempoTotal.toFloat()

    // Mínimo 1 punto si acierta en el último milisegundo
    return (puntosBase * factorTiempo).toInt().coerceAtLeast(1)
}

enum class EstadoRespuesta { CORRECTA, INCORRECTA }

@Composable
fun OpcionCard(
    texto: String,
    seleccionada: Boolean,
    estadoRespuesta: EstadoRespuesta?,
    esLaCorrecta: Boolean,
    onClick: () -> Unit
) {
    val colorBorde = when {
        // Seleccionada y Correcta -> Verde
        seleccionada && estadoRespuesta == EstadoRespuesta.CORRECTA -> Color(0xFF4CAF50)
        // Seleccionada e Incorrecta -> Rojo
        seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA -> Color(0xFFF44336)
        // No seleccionada, pero era la correcta (mostrar solución) -> Verde
        !seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA && esLaCorrecta -> Color(0xFF4CAF50)
        // Por defecto -> Gris/Outline del tema
        else -> MaterialTheme.colorScheme.outline
    }

    val colorFondo = if (estadoRespuesta != null && seleccionada) colorBorde.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(2.dp, colorBorde, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun QuizResultScreen(
    puntuacion: Int,
    totalPreguntas: Int,
    onVolverAlMenu: () -> Unit,
    onNavigateToRanking: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.kids_quiz_finalizado),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Has conseguido $puntuacion puntos!",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onVolverAlMenu,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.kids_quiz_volver_menu))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateToRanking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.kids_quiz_ranking))
        }
    }
}