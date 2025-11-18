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
import com.example.mariamolina.R
import com.example.mariamolina.data.model.Dificultad
import com.example.mariamolina.data.model.OpcionRespuesta
import com.example.mariamolina.data.model.quizMockData
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun QuizGameScreen(
    dificultad: Dificultad,
    onQuizFinished: () -> Unit,
    onNavigateToRanking: () -> Unit
) {
    val preguntas by remember(dificultad) {
        mutableStateOf(
            quizMockData
                .filter { it.dificultad == dificultad }
                .shuffled()
        )
    }

    val tiempoTotalPorPregunta = 20000L
    var tiempoRestante by remember { mutableStateOf(tiempoTotalPorPregunta) }

    var puntuacion by remember { mutableStateOf(0) }
    var indicePreguntaActual by remember { mutableStateOf(0) }
    var respuestaSeleccionada by remember { mutableStateOf<OpcionRespuesta?>(null) }
    var estadoRespuesta by remember { mutableStateOf<EstadoRespuesta?>(null) }

    LaunchedEffect(key1 = indicePreguntaActual) {
        tiempoRestante = tiempoTotalPorPregunta

        while (tiempoRestante > 0 && estadoRespuesta == null) {
            delay(100L)
            tiempoRestante -= 100L
        }

        if (estadoRespuesta == null && tiempoRestante <= 0) {
            estadoRespuesta = EstadoRespuesta.INCORRECTA
            respuestaSeleccionada = null
        }
    }

    LaunchedEffect(key1 = estadoRespuesta) {
        if (estadoRespuesta != null) {
            delay(1500)
            indicePreguntaActual++
            respuestaSeleccionada = null
            estadoRespuesta = null
        }
    }

    if (indicePreguntaActual >= preguntas.size) {
        QuizResultScreen(
            puntuacion = puntuacion,
            totalPreguntas = preguntas.size,
            onVolverAlMenu = onQuizFinished,
            onNavigateToRanking = onNavigateToRanking
        )
    } else {
        val preguntaActual = preguntas[indicePreguntaActual]

        val opcionesAleatorias by remember(preguntaActual) {
            mutableStateOf(preguntaActual.opciones.shuffled())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Parte Superior: Puntos, Barra y RELOJ
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
                    text = stringResource(id = preguntaActual.preguntaResId),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Parte Central: Opciones
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                opcionesAleatorias.forEach { opcion ->
                    OpcionCard(
                        texto = stringResource(id = opcion.textoResId),
                        seleccionada = respuestaSeleccionada == opcion,
                        estadoRespuesta = if (respuestaSeleccionada == opcion) estadoRespuesta else null,
                        esLaCorrecta = opcion.esCorrecta,
                        onClick = {
                            if (estadoRespuesta == null) {
                                respuestaSeleccionada = opcion
                                if (opcion.esCorrecta) {
                                    val puntosBase = 1000
                                    val porcentajeTiempo = tiempoRestante.toFloat() / tiempoTotalPorPregunta.toFloat()
                                    val puntosGanados = (puntosBase * porcentajeTiempo).toInt()

                                    puntuacion += puntosGanados
                                    estadoRespuesta = EstadoRespuesta.CORRECTA
                                } else {
                                    estadoRespuesta = EstadoRespuesta.INCORRECTA
                                }
                            }
                        }
                    )
                }
            }

            // Espacio de relleno al final
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
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
        seleccionada && estadoRespuesta == EstadoRespuesta.CORRECTA -> Color(0xFF4CAF50)
        seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA -> Color(0xFFF44336)
        !seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA && esLaCorrecta -> Color(0xFF4CAF50)
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