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
    pinPartida: String? = null, // Recibimos el PIN para multijugador
    onQuizFinished: () -> Unit,
    onNavigateToRanking: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val preguntas = uiState.questions
    val puntuacion = uiState.puntuacion
    val indicePreguntaActual = uiState.indicePreguntaActual
    val estadoPartida = uiState.estadoPartida // Estado global (JUGANDO / RESULTADOS)

    // Carga inicial de preguntas
    LaunchedEffect(key1 = dificultad) {
        // Si tenemos PIN, nos conectamos a la partida multijugador
        if (pinPartida != null) {
            viewModel.conectarAPartida(pinPartida, dificultad)
        }
        // Si no, cargamos preguntas locales (modo solitario)
        else if (preguntas.isEmpty()) {
            viewModel.loadQuestions(dificultad)
        }
    }

    // --- ESTADO LOCAL (Temporizador y selección) ---
    val tiempoTotalPorPregunta = 20000L
    var tiempoRestante by remember(indicePreguntaActual) { mutableStateOf(tiempoTotalPorPregunta) }
    var respuestaSeleccionada by remember { mutableStateOf<OpcionRespuesta?>(null) }

    // Controlamos si el usuario ya respondió localmente
    val haRespondidoLocalmente = respuestaSeleccionada != null

    // Si es multijugador, esperamos a que el estado cambie a RESULTADOS.
    // Si es solitario, mostramos resultados inmediatamente al responder.
    val mostrarResultados = if (pinPartida != null) estadoPartida == EstadoPartida.RESULTADOS else haRespondidoLocalmente

    // --- LÓGICA DEL TEMPORIZADOR ---
    // Se reinicia con cada nueva pregunta o cuando llegan los datos (preguntas.size)
    // Solo corre si estamos JUGANDO y no hemos respondido
    LaunchedEffect(indicePreguntaActual, haRespondidoLocalmente, preguntas.size, estadoPartida) {
        val estamosJugando = if (pinPartida != null) estadoPartida == EstadoPartida.JUGANDO else true

        if (indicePreguntaActual < preguntas.size && !haRespondidoLocalmente && estamosJugando) {
            // Aseguramos reinicio si venimos de carga o cambio de pregunta
            if (tiempoRestante <= 0) tiempoRestante = tiempoTotalPorPregunta

            while (tiempoRestante > 0 && !haRespondidoLocalmente) {
                delay(100L)
                tiempoRestante -= 100L
            }
            // Si el tiempo llega a 0, se considera respondido (incorrectamente/tiempo agotado)
            // En solitario esto forzaría el avance, en multi esperamos al profe.
        }
    }

    // --- GESTIÓN DE PANTALLAS (Carga, Error, Juego, Fin) ---

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Error: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    // --- FIN DEL JUEGO ---
    // Condición de fin: se acabaron las preguntas O el estado es FINALIZADO
    if ((preguntas.isNotEmpty() && indicePreguntaActual >= preguntas.size) || estadoPartida == EstadoPartida.FINALIZADO) {
        // Guardamos puntuación en Firebase si es multijugador
        LaunchedEffect(Unit) {
            if (pinPartida != null) {
                viewModel.guardarPuntuacion(pinPartida)
            }
        }

        // Limpiamos al salir
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

    // --- PANTALLA DE JUEGO ---
    if (preguntas.isNotEmpty()) {
        // Protección de índice por si acaso
        val indiceSeguro = indicePreguntaActual.coerceIn(0, preguntas.lastIndex)
        val preguntaActual = preguntas[indiceSeguro]

        // Mezclamos opciones una vez por pregunta
        val opcionesAleatorias by remember(preguntaActual) {
            mutableStateOf(preguntaActual.opciones.shuffled())
        }

        // --- PANTALLA DE ESPERA (SOLO MULTIPLAYER) ---
        // Si ya respondimos pero el profe no ha pasado a RESULTADOS
        if (pinPartida != null && haRespondidoLocalmente && !mostrarResultados) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "¡Respuesta enviada!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Esperando a que todos terminen...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // --- PANTALLA NORMAL (PREGUNTA O RESULTADO) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Scroll para pantallas pequeñas
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Encabezado
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

                    // Reloj
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

                    Text(
                        text = preguntaActual.pregunta,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }

                // Opciones
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    opcionesAleatorias.forEach { opcion ->
                        // Lógica visual:
                        // Si mostramos resultados, coloreamos Correcta (Verde) e Incorrecta (Rojo)
                        // Si estamos jugando, solo marcamos la seleccionada (Gris/Azul)

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
                                // Solo permitimos responder si no hemos respondido ya
                                if (!haRespondidoLocalmente) {
                                    respuestaSeleccionada = opcion

                                    // Calcular y enviar puntos inmediatamente
                                    val puntosGanados = if (opcion.esCorrecta) {
                                        calcularPuntos(opcion, tiempoRestante, tiempoTotalPorPregunta)
                                    } else 0

                                    viewModel.procesarRespuesta(puntosGanados, pinPartida)

                                    // Si es modo solitario, avanzamos automáticamente tras delay
                                    // (Esto requiere un LaunchedEffect adicional, pero para mantenerlo simple,
                                    // en solitario el usuario ve el feedback y luego avanza.
                                    // Aquí simplificamos para que la lógica principal sea Multiplayer).
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

// --- LÓGICA AUXILIAR ---

private fun calcularPuntos(respuesta: OpcionRespuesta?, tiempoRestante: Long, tiempoTotal: Long): Int {
    if (respuesta?.esCorrecta != true) return 0

    val puntosBase = 1000
    val factorTiempo = tiempoRestante.toFloat() / tiempoTotal.toFloat()
    // Mínimo 1 punto si acierta
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
        // No seleccionada, pero era la correcta (mostrar solución al final) -> Verde
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