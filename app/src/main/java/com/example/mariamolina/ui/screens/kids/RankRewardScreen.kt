package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Datos de un rango medieval basado en el percentil del jugador.
 */
private data class RangoMedieval(
    val nombre: String,
    val imagenUrl: String,
    val descripcion: String
)

/**
 * Obtiene el rango medieval basado en el percentil del jugador.
 * @param percentil Valor de 0 a 100 donde 0 = mejor, 100 = peor
 *                  (percentil = posición / total * 100)
 */
private fun obtenerRangoPorPercentil(percentil: Float): RangoMedieval {
    return when {
        percentil <= 20f -> RangoMedieval(
            nombre = "Capitán de la Guardia Real",
            imagenUrl = "https://ik.imagekit.io/fn2wdosiw/CapitanGuardiaReal-Photoroom.png?updatedAt=1764250963029",
            descripcion = "¡Felicidades! María de Molina te ha nombrado Capitán de su Guardia Real, un honor reservado solo para los más sabios y valientes del reino. Tu dominio sobre su historia es impecable."
        )
        percentil <= 40f -> RangoMedieval(
            nombre = "Caballero",
            imagenUrl = "https://ik.imagekit.io/fn2wdosiw/Caballero-Photoroom.png?updatedAt=1764250963003",
            descripcion = "¡Enhorabuena, ahora eres Caballero de María de Molina! Todavía puedes mejorar pero tienes un conocimiento bastante alto."
        )
        percentil <= 60f -> RangoMedieval(
            nombre = "Pregonero Real",
            imagenUrl = "https://ik.imagekit.io/fn2wdosiw/PregoneroReal-Photoroom.png?updatedAt=1764250962964",
            descripcion = "¡Ahora eres Pregonero Real de María de Molina! Conoces bien su historia y puedes llevar sus palabras por el reino. ¡Sigue profundizando para convertirte en un verdadero experto!"
        )
        percentil <= 80f -> RangoMedieval(
            nombre = "Paje",
            imagenUrl = "https://ik.imagekit.io/fn2wdosiw/Paje-Photoroom.png?updatedAt=1764250962935",
            descripcion = "Te has ganado un hueco en palacio, aunque aún te toca llevar mensajes y cargar capas. ¡Un poco más de esfuerzo y ascenderás rápidamente!"
        )
        else -> RangoMedieval(
            nombre = "Campesino",
            imagenUrl = "https://ik.imagekit.io/fn2wdosiw/Campesino-Photoroom.png?updatedAt=1764250962943",
            descripcion = "¡Eres Campesino de María de Molina! Todavía tienes mucho por aprender de ella... ¡anímate a conocer su historia!"
        )
    }
}

/**
 * Pantalla que muestra el rango obtenido por el jugador antes del ranking final.
 * 
 * Para modo MULTIJUGADOR: usa posición y total de jugadores
 * Para modo SOLITARIO: usa aciertos y total de preguntas
 * 
 * @param posicion Posición del jugador (1-indexed) o número de aciertos en modo solitario
 * @param total Total de jugadores o total de preguntas en modo solitario
 * @param puntuacion Puntuación total obtenida
 * @param esModoSolitario Si true, calcula percentil basado en aciertos/preguntas
 * @param onContinue Callback para continuar al ranking
 */
@Composable
fun RankRewardScreen(
    posicion: Int,
    total: Int,
    puntuacion: Int,
    esModoSolitario: Boolean = false,
    onContinue: () -> Unit
) {
    // Calcular percentil
    // En multijugador: percentil = (posición / total) * 100 → más bajo = mejor
    // En solitario: percentil = ((total - aciertos) / total) * 100 → más aciertos = mejor (percentil más bajo)
    val percentil = if (esModoSolitario) {
        // En modo solitario: aciertos = posicion, total = preguntas totales
        // Si aciertas todas → percentil 0 (mejor)
        // Si no aciertas ninguna → percentil 100 (peor)
        ((total - posicion).toFloat() / total.coerceAtLeast(1)) * 100f
    } else {
        // En modo multijugador: posición 1 de 10 → percentil 10 (mejor)
        (posicion.toFloat() / total.coerceAtLeast(1)) * 100f
    }
    
    val rango = obtenerRangoPorPercentil(percentil)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = "¡Has obtenido un rango!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Imagen del rango
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = rango.imagenUrl,
                    contentDescription = rango.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del rango
            Text(
                text = rango.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )

            // Card con puntuación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (esModoSolitario) {
                        Text(
                            text = "Aciertos: $posicion / $total",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Posición",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "#$posicion",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Puntuación",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$puntuacion pts",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Descripción del rango
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = rango.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón continuar
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (esModoSolitario) "Volver al Menú" else "Ver Ranking Final",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
