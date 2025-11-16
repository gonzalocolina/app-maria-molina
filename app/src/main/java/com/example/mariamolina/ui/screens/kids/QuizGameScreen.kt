package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun QuizGameScreen(
    dificultad: Dificultad,
    onQuizFinished: () -> Unit,
    onNavigateToRanking: () -> Unit // ¡CAMBIO! Añadido
) {
    // 1. Preparamos las preguntas
    val preguntas by remember(dificultad) {
        mutableStateOf(
            quizMockData
                .filter { it.dificultad == dificultad }
                .shuffled()
        )
    }

    var puntuacion by remember { mutableStateOf(0) }
    var indicePreguntaActual by remember { mutableStateOf(0) }
    var respuestaSeleccionada by remember { mutableStateOf<OpcionRespuesta?>(null) }
    var estadoRespuesta by remember { mutableStateOf<EstadoRespuesta?>(null) }

    if (indicePreguntaActual >= preguntas.size) {
        // --- Pantalla de Fin de Juego ---
        QuizResultScreen(
            puntuacion = puntuacion,
            totalPreguntas = preguntas.size,
            onVolverAlMenu = onQuizFinished,
            onNavigateToRanking = onNavigateToRanking // ¡CAMBIO! Se lo pasamos
        )
    } else {
        // --- Pantalla de Juego ---
        val preguntaActual = preguntas[indicePreguntaActual]

        val opcionesAleatorias by remember(preguntaActual) {
            mutableStateOf(preguntaActual.opciones.shuffled())
        }

        LaunchedEffect(key1 = estadoRespuesta) {
            if (estadoRespuesta != null) {
                delay(1500)
                indicePreguntaActual++
                respuestaSeleccionada = null
                estadoRespuesta = null
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Superior: Puntuación y Pregunta
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
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(id = preguntaActual.preguntaResId),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Medio: Opciones de respuesta
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
                                    estadoRespuesta = EstadoRespuesta.CORRECTA
                                    puntuacion++
                                } else {
                                    estadoRespuesta = EstadoRespuesta.INCORRECTA
                                }
                            }
                        }
                    )
                }
            }

            // Inferior: Botón
            Button(
                onClick = { /* El LaunchedEffect hace el trabajo */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.kids_quiz_siguiente))
            }
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
        seleccionada && estadoRespuesta == EstadoRespuesta.CORRECTA -> Color(0xFF4CAF50) // Verde
        seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA -> Color(0xFFF44336) // Rojo
        !seleccionada && estadoRespuesta == EstadoRespuesta.INCORRECTA && esLaCorrecta -> Color(0xFF4CAF50) // Verde
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

// Pantalla de resultados
@Composable
fun QuizResultScreen(
    puntuacion: Int,
    totalPreguntas: Int,
    onVolverAlMenu: () -> Unit,
    onNavigateToRanking: () -> Unit // ¡CAMBIO! Añadido
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.kids_quiz_finalizado),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.kids_quiz_resultado, puntuacion, totalPreguntas),
            style = MaterialTheme.typography.titleLarge
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
            onClick = onNavigateToRanking, // ¡CAMBIO! Conectado
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.kids_quiz_ranking))
        }
    }
}