package com.example.mariamolina.ui.screens.kids

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mariamolina.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.example.mariamolina.data.model.Slide
import com.example.mariamolina.data.model.SlidesProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign

@Composable
fun KidsSlidesScreen(
    onBackToEntry: () -> Unit,
    slides: List<Slide> = SlidesProvider.testSlides
) {
    // Estado del índice actual
    var currentIndex by remember { mutableStateOf(0) }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(0.dp)) {

        if (slides.isEmpty()) {
            // Mensaje si no hay diapositivas
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(id = R.string.kids_slides_titulo), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(id = R.string.kids_no_slides), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            val slide = slides.getOrNull(currentIndex) ?: slides.first()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen con detección de arrastre horizontal
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            // dragAmount positivo = arrastre a la derecha (quieres ir al slide anterior)
                            if (dragAmount > 20) {
                                // mover atrás
                                currentIndex = (currentIndex - 1).coerceAtLeast(0)
                                change.consume()
                            } else if (dragAmount < -20) {
                                // mover adelante
                                currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex)
                                change.consume()
                            }
                        }
                    }
                ) {
                    // Cargar imagen desde drawable
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val imageResId = remember(slide.imageUrl) {
                        context.resources.getIdentifier(slide.imageUrl, "drawable", context.packageName).let {
                            if (it != 0) it else R.drawable.mariademolina_photoroom
                        }
                    }

                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = slide.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Botón atrás flotante en esquina superior izquierda
                    IconButton(
                        onClick = onBackToEntry,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cd_back), tint = Color.White)
                    }

                    // Botón anterior (centro-izquierda)
                    if (currentIndex > 0) {
                        IconButton(
                            onClick = { currentIndex = (currentIndex - 1).coerceAtLeast(0) },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anterior", tint = Color.White)
                        }
                    }

                    // Botón siguiente (centro-derecha)
                    if (currentIndex < slides.lastIndex) {
                        IconButton(
                            onClick = { currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex) },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente", tint = Color.White)
                        }
                    }

                    // Mostrar texto según el tipo de diapositiva
                    if (slide.hasSpeechBubble) {
                        // Bocadillo de diálogo (speech bubble) en la parte inferior
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.85f)
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(3.dp, Color(0xFF6200EE))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = slide.description,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // Texto simple sin bocadillo
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = slide.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = slide.description, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Indicadores de páginas (puntos)
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    slides.forEachIndexed { index, _ ->
                        val selected = index == currentIndex
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (selected) 10.dp else 8.dp)
                                .background(
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para volver o finalizar
                Button(onClick = onBackToEntry, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(stringResource(id = R.string.cd_back))
                }
            }
        }
    }
}
