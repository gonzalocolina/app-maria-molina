@file:SuppressLint("DiscouragedApi")
package com.edunova.mariamolina.ui.screens.kids

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.edunova.mariamolina.R
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.edunova.mariamolina.data.model.Slide
import com.edunova.mariamolina.data.model.SlidesProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

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

    // Umbral de arrastre (en px) — usado para decidir un único avance por gesto
    val dragThresholdPx = with(LocalDensity.current) { 40.dp.toPx() }

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
                // Layout horizontal: imagen a la izquierda, texto e indicadores a la derecha
                // Calculamos la mitad del ancho de pantalla para que el panel de texto llegue hasta el centro
                // Usamos LocalWindowInfo.current.containerSize (en px) si está disponible y lo convertimos a dp.
                val halfWidth = run {
                    val windowInfo = LocalWindowInfo.current
                    // LocalWindowInfo.current es no-nulo en esta versión; usamos su containerSize (en px)
                    // y lo convertimos a dp. Dividimos por 2 para obtener la mitad de la pantalla.
                    val pxWidth = windowInfo.containerSize.width
                    val widthDp = with(LocalDensity.current) { pxWidth.toDp() }
                    widthDp / 2
                }
                // Espacio reservado para el botón derecho para evitar solapamiento con el texto
                val buttonSpace = 80.dp

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Lado izquierdo: imagen y botones de navegación sobre la imagen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                // Implementación por-gesto: acumulamos deltaX y aplicamos exactamente
                                // un avance/retroceso por gesto cuando se supera el umbral.
                                while (true) {
                                    awaitPointerEventScope {
                                        // Esperamos eventos; no usamos awaitFirstDown() por compatibilidad
                                        var totalDragX = 0f
                                        var movedThisGesture = false
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull() ?: break
                                            val dx = change.position.x - change.previousPosition.x
                                            if (dx != 0f) {
                                                totalDragX += dx
                                                if (!movedThisGesture) {
                                                    if (totalDragX > dragThresholdPx) {
                                                        currentIndex = (currentIndex - 1).coerceAtLeast(0)
                                                        movedThisGesture = true
                                                        event.changes.forEach { it.consume() }
                                                    } else if (totalDragX < -dragThresholdPx) {
                                                        currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex)
                                                        movedThisGesture = true
                                                        event.changes.forEach { it.consume() }
                                                    }
                                                }
                                            }
                                            // Fin de gesto cuando ya no hay punteros presionados
                                            if (!event.changes.any { it.pressed }) break
                                        }
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
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = stringResource(id = slide.titleRes),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            val imageResId = remember(imageUrl) { resolveDrawableId(ctx, imageUrl) }
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = stringResource(id = slide.titleRes),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Botón atrás flotante en esquina superior izquierda (sobre la imagen)
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

                        // Nota: botón 'Anterior' interno removido para evitar duplicados; los controles Prev/Next se dibujan
                        // en el Box padre para que estén siempre centrados verticalmente.
                    }

                    // Lado derecho: panel de texto e indicadores — ocupará hasta el centro de la pantalla
                    // y deja un padding end suficiente para el botón.
                    Column(
                        modifier = Modifier
                            .width(halfWidth)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.03f))
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = buttonSpace),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Contenido principal: título y descripción / bocadillo
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(id = slide.titleRes), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (slide.hasSpeechBubble) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(2.dp, Color(0xFF6200EE))
                                ) {
                                    Text(
                                        text = stringResource(id = slide.descriptionRes),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = stringResource(id = slide.descriptionRes), color = Color.White, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // NOTE: controles Prev/Next removidos de aquí (mostraremos sólo los extremos)
                        }

                        // Indicadores y número de slide en la parte inferior
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                slides.forEachIndexed { index, _ ->
                                    val selected = index == currentIndex
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(if (selected) 12.dp else 8.dp)
                                            .background(
                                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = stringResource(id = R.string.slide_counter, currentIndex + 1, slides.size), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Ahora dibujamos los botones Prev/Next a nivel del Box contenedor (heredado por la composición) para
                // que estén alineados al centro vertical de la pantalla y a los extremos horizontales.
                if (currentIndex > 0) {
                    IconButton(
                        onClick = { currentIndex = (currentIndex - 1).coerceAtLeast(0) },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                            .size(56.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = stringResource(id = R.string.kids_prev), tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                if (currentIndex < slides.lastIndex) {
                    IconButton(
                        onClick = { currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                            .size(56.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = stringResource(id = R.string.kids_next), tint = Color.White, modifier = Modifier.size(32.dp))
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
                            // Implementación por-gesto (misma lógica que en landscape): acumulamos deltaX
                            // y aplicamos exactamente un avance/retroceso por gesto cuando se supera el umbral.
                            while (true) {
                                awaitPointerEventScope {
                                    // Esperamos eventos; no usamos awaitFirstDown() por compatibilidad
                                    var totalDragX = 0f
                                    var movedThisGesture = false
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        val dx = change.position.x - change.previousPosition.x
                                        if (dx != 0f) {
                                            totalDragX += dx
                                            if (!movedThisGesture) {
                                                if (totalDragX > dragThresholdPx) {
                                                    currentIndex = (currentIndex - 1).coerceAtLeast(0)
                                                    movedThisGesture = true
                                                    event.changes.forEach { it.consume() }
                                                } else if (totalDragX < -dragThresholdPx) {
                                                    currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex)
                                                    movedThisGesture = true
                                                    event.changes.forEach { it.consume() }
                                                }
                                            }
                                        }
                                        // Fin de gesto cuando ya no hay punteros presionados
                                        if (!event.changes.any { it.pressed }) break
                                    }
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
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = stringResource(id = slide.titleRes),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            val imageResId = remember(imageUrl) { resolveDrawableId(ctx, imageUrl) }
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = stringResource(id = slide.titleRes),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Botón atrás flotante en esquina superior izquierda (sobre la imagen)
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Panel de texto e indicadores
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.03f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Contenido principal: título y descripción / bocadillo
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(id = slide.titleRes), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (slide.hasSpeechBubble) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(2.dp, Color(0xFF6200EE))
                                ) {
                                    Text(
                                        text = stringResource(id = slide.descriptionRes),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = stringResource(id = slide.descriptionRes), color = Color.White, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // NOTE: controles Prev/Next removidos de aquí (mostraremos sólo los extremos)
                        }

                        // Indicadores y número de slide en la parte inferior
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                slides.forEachIndexed { index, _ ->
                                    val selected = index == currentIndex
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(if (selected) 12.dp else 8.dp)
                                            .background(
                                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = stringResource(id = R.string.slide_counter, currentIndex + 1, slides.size), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Ahora dibujamos los botones Prev/Next a nivel del Box contenedor (heredado por la composición) para
                // que estén alineados al centro vertical de la pantalla y a los extremos horizontales.
                if (currentIndex > 0) {
                    IconButton(
                        onClick = { currentIndex = (currentIndex - 1).coerceAtLeast(0) },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                            .size(56.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = stringResource(id = R.string.kids_prev), tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                if (currentIndex < slides.lastIndex) {
                    IconButton(
                        onClick = { currentIndex = (currentIndex + 1).coerceAtMost(slides.lastIndex) },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
                            .size(56.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = stringResource(id = R.string.kids_next), tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
