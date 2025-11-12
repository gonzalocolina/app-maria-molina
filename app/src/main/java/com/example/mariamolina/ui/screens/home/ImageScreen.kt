package com.example.mariamolina.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .onSizeChanged { containerSize = it } // guardamos tamaño para los cálculos
                .clipToBounds() // importante para que no se dibuje fuera
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // actualizar escala
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                        // cuando cambia la escala, ajustamos offset para que siga dentro de límites
                        scale = newScale

                        // actualizar offset con el pan (ya en px)
                        offset += pan

                        // calcular límites máximos en cada eje
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
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBackClick) {
            Text("Volver")
        }
    }
}
