package com.example.mariamolina.ui.screens.kids

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
import androidx.compose.ui.unit.dp
import com.example.mariamolina.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import com.example.mariamolina.data.model.Slide
import com.example.mariamolina.data.model.SlidesProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.foundation.shape.RoundedCornerShape as _RoundedCornerShape

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
                    .height(300.dp)
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
                    // Placeholder de imagen (caja gris) en lugar de cargar desde red
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFCCCCCC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = stringResource(id = R.string.cd_imagen_ejemplo), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = slide.imageUrl, style = MaterialTheme.typography.bodySmall)
                        }
                    }

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

                    // Título sobre la imagen (parte inferior)
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = slide.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = slide.description, color = Color.White, style = MaterialTheme.typography.bodyMedium)
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
