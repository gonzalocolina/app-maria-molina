package com.example.mariamolina.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import com.example.mariamolina.R
import kotlin.math.max

@Composable
fun ImageScreen(onBackClick: () -> Unit) {
    val painter = painterResource(id = R.drawable.arbol)

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) } // en px
    var containerSize by remember { mutableStateOf(IntSize.Zero) } // tamaño del área de la imagen

    // límites de zoom base
    val baseMinScale = 1f
    val baseMaxScale = 4f

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidthDp = this.maxWidth
        val isTablet = maxWidthDp >= 600.dp

        // Si entramos en modo tablet vertical, reseteamos zoom/offset para mostrar la imagen completa
        LaunchedEffect(isTablet, isLandscape) {
            if (isTablet && !isLandscape) {
                scale = 1f
                offset = Offset.Zero
            }
        }

        // Ajustamos contentScale según tamaño/orientación (se usará por defecto)
        val imageContentScaleDefault = ContentScale.Fit

        // Ajustamos maxScale para que no sea excesivo en pantallas grandes
        val maxScale = if (isTablet) {
            // En tablets, permitir menos zoom relativo (porque la imagen ya entra bien en pantalla)
            2.5f
        } else {
            baseMaxScale
        }

        // Si estamos en tablet en vertical queremos mostrar la imagen encima del contenido
        // y que se vea completa: usamos fillMaxWidth + aspectRatio del painter (si está disponible)
        if (isTablet && !isLandscape) {
            // Intentamos obtener aspect ratio del painter
            val intrinsic = painter.intrinsicSize
            val aspectRatio = if (intrinsic.width > 0f && intrinsic.height > 0f) {
                intrinsic.width / intrinsic.height
            } else null

            // Contenedor que ajusta su altura para mostrar la imagen completa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (aspectRatio != null) Modifier.aspectRatio(aspectRatio) else Modifier.height(400.dp))
                    .onSizeChanged { containerSize = it }
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(baseMinScale, maxScale)

                            if (newScale == baseMinScale) {
                                scale = newScale
                                offset = Offset.Zero
                            } else {
                                scale = newScale
                                offset += pan
                            }

                            val containerWidth = containerSize.width.toFloat()
                            val containerHeight = containerSize.height.toFloat()
                            if (containerWidth > 0f && containerHeight > 0f) {
                                val scaledWidth = containerWidth * scale
                                val scaledHeight = containerHeight * scale

                                val maxX = max(0f, (scaledWidth - containerWidth) / 2f)
                                val maxY = max(0f, (scaledHeight - containerHeight) / 2f)

                                val clampedX = offset.x.coerceIn(-maxX, maxX)
                                val clampedY = offset.y.coerceIn(-maxY, maxY)
                                offset = Offset(clampedX, clampedY)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.cd_imagen_ejemplo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )
            }

        } else {
            // Comportamiento original (imagen a pantalla completa con gestos)
            Box(
                modifier = Modifier
                    .fillMaxSize() // La imagen ocupará todo el espacio disponible
                    .onSizeChanged { containerSize = it }
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(baseMinScale, maxScale)

                            // Si volvemos a la escala mínima, centramos la imagen
                            if (newScale == baseMinScale) {
                                scale = newScale
                                offset = Offset.Zero
                            } else {
                                scale = newScale
                                offset += pan
                            }

                            val containerWidth = containerSize.width.toFloat()
                            val containerHeight = containerSize.height.toFloat()
                            if (containerWidth > 0f && containerHeight > 0f) {
                                val scaledWidth = containerWidth * scale
                                val scaledHeight = containerHeight * scale

                                val maxX = max(0f, (scaledWidth - containerWidth) / 2f)
                                val maxY = max(0f, (scaledHeight - containerHeight) / 2f)

                                val clampedX = offset.x.coerceIn(-maxX, maxX)
                                val clampedY = offset.y.coerceIn(-maxY, maxY)
                                offset = Offset(clampedX, clampedY)
                            }
                        }
                    }
            ) {
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.cd_imagen_ejemplo),
                    contentScale = imageContentScaleDefault,
                    modifier = Modifier
                        .fillMaxSize() // La imagen se expandirá para llenar el Box manteniendo el aspecto
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )
            }
        }

        // Botón "Volver" alineado en la parte inferior
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.BottomCenter) // Alineamos el botón en la parte inferior central
                .padding(16.dp) // Añadimos padding alrededor del botón
        ) {
            Text(stringResource(R.string.btn_volver))
        }
    }
}
