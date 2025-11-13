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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import com.example.mariamolina.R
import kotlin.math.max

@Composable
fun ImageScreen(onBackClick: () -> Unit) {
    val painter = painterResource(id = R.drawable.arbol)

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) } // en px
    var containerSize by remember { mutableStateOf(IntSize.Zero) } // tamaño del área de la imagen

    // límites de zoom
    val minScale = 1f
    val maxScale = 4f

    Box(modifier = Modifier.fillMaxSize()) { // Usamos Box para superponer la imagen y el botón
        Box(
            modifier = Modifier
                .fillMaxSize() // La imagen ocupará todo el espacio disponible
                .onSizeChanged { containerSize = it }
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                        scale = newScale

                        offset += pan

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
                contentDescription = "Imagen de ejemplo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize() // La imagen se expandirá para llenar el Box
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )
        }

        // Botón "Volver" alineado en la parte inferior
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.BottomCenter) // Alineamos el botón en la parte inferior central
                .padding(16.dp) // Añadimos padding alrededor del botón
        ) {
            Text("Volver")
        }
    }
}
