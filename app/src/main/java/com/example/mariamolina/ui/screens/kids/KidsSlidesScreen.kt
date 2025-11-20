@file:SuppressLint("DiscouragedApi")
package com.example.mariamolina.ui.screens.kids

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mariamolina.R
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.mariamolina.data.model.Slide
import com.example.mariamolina.data.model.SlidesProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign

// Resuelve el nombre de recurso drawable a un id usando el Context en runtime.
// Si no existe, retorna un drawable del sistema como fallback.
private fun resolveDrawableId(context: Context, name: String): Int {
    // Si el nombre contiene '/' o 'http' no es un nombre de recurso, retornamos 0 y usaremos fallback
    val sanitized = name.substringAfterLast('/').substringBefore('?')
    val resId = context.resources.getIdentifier(sanitized, "drawable", context.packageName)
    return if (resId != 0) resId else android.R.drawable.ic_menu_report_image
}

@Composable
fun KidsSlidesScreen(
    onBackToEntry: () -> Unit,
    slides: List<Slide> = SlidesProvider.testSlides
) {
    // Estado del índice currente
    var currentIndex by remember { mutableIntStateOf(0) }

    // Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier
        .fillMaxSize()) {

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

            if (isLandscape) {
                // Layout horizontal: controles en la parte derecha
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Imagen ocupa el espacio principal
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                if (dragAmount > 20) {
                                    currentIndex = (currentIndex - 1).coerceAtLeast(0)
                                    change.consume()
                                } else if (dragAmount < -20) {
                                    currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex)
                                    change.consume()
                                }
                            }
                        }
                    ) {
                        // Cargar imagen: si `imageUrl` es una URL -> usar Coil AsyncImage; si no -> cargar drawable mapeado
                        val ctx = LocalContext.current
                        val imageUrl = slide.imageUrl
                        if (imageUrl.startsWith("http") || imageUrl.contains("://")) {
                            AsyncImage(
                                model = ImageRequest.Builder(ctx)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .error(resolveDrawableId(ctx, imageUrl))
                                    .placeholder(resolveDrawableId(ctx, imageUrl))
                                    .build(),
                                contentDescription = slide.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            val imageResId = remember(imageUrl) { resolveDrawableId(ctx, imageUrl) }
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = slide.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Botón atrás flotante en esquina superior izquierda
                        IconButton(
                            onClick = onBackToEntry,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                .size(40.dp)
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
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronLeft,
                                    contentDescription = "Anterior",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Botón siguiente (centro-derecha)
                        if (currentIndex < slides.lastIndex) {
                            IconButton(
                                onClick = { currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex) },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 8.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Siguiente",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Mostrar texto según el tipo de diapositiva (versión compacta)
                        if (slide.hasSpeechBubble) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(0.9f)
                                    .padding(bottom = 12.dp, start = 40.dp, end = 40.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color(0xFF6200EE))
                            ) {
                                Text(
                                    text = slide.description,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = slide.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = slide.description, color = Color.White, style = MaterialTheme.typography.bodySmall, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Panel lateral derecho con indicadores
                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Indicadores de páginas (puntos) verticales
                        slides.forEachIndexed { index, _ ->
                            val selected = index == currentIndex
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 3.dp)
                                    .size(if (selected) 12.dp else 8.dp)
                                    .background(
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            } else {
                // Layout vertical original
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
                        // Cargar imagen: si `imageUrl` es una URL -> usar Coil AsyncImage; si no -> cargar drawable mapeado
                        val ctx2 = LocalContext.current
                        val imageUrl2 = slide.imageUrl
                        if (imageUrl2.startsWith("http") || imageUrl2.contains("://")) {
                            AsyncImage(
                                model = ImageRequest.Builder(ctx2)
                                    .data(imageUrl2)
                                    .crossfade(true)
                                    .error(resolveDrawableId(ctx2, imageUrl2))
                                    .placeholder(resolveDrawableId(ctx2, imageUrl2))
                                    .build(),
                                contentDescription = slide.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            val imageResId2 = remember(imageUrl2) { resolveDrawableId(ctx2, imageUrl2) }
                            Image(
                                painter = painterResource(id = imageResId2),
                                contentDescription = slide.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
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
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronLeft,
                                    contentDescription = "Anterior",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Botón siguiente (centro-derecha)
                        if (currentIndex < slides.lastIndex) {
                            IconButton(
                                onClick = { currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex) },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 8.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Siguiente",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
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
}
