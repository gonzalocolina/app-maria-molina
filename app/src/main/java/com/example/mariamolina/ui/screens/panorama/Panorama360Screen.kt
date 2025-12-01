package com.example.mariamolina.ui.screens.panorama

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mariamolina.R

/**
 * Pantalla de visualización panorámica 360° del Monasterio.
 * Utiliza OpenGL ES para renderizar la imagen equirectangular en una esfera 3D.
 */
@Composable
fun Panorama360Screen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Crear renderer y vista OpenGL
    val renderer = remember {
        SphericalRenderer(context, R.drawable.foto_monasterio360)
    }

    val glSurfaceView = remember {
        Panorama360GLSurfaceView(context, renderer)
    }

    // Manejar el ciclo de vida de GLSurfaceView
    DisposableEffect(Unit) {
        onDispose {
            glSurfaceView.onPause()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Vista OpenGL 360°
            AndroidView(
                factory = { glSurfaceView },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay con controles
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Barra superior con botón de volver y título
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    // Botón de volver
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_volver),
                            tint = Color.White
                        )
                    }

                    // Título centrado
                    Text(
                        text = stringResource(R.string.panorama_screen_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Espacio flexible para empujar los controles hacia abajo
                Box(modifier = Modifier.weight(1f))

                // Instrucciones de uso (solo en portrait para no ocupar espacio)
                if (!isLandscape) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.panorama_instructions),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Controles de zoom en la esquina inferior derecha
                Column(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botón de zoom in
                    IconButton(
                        onClick = {
                            renderer.zoom = (renderer.zoom * 1.2f).coerceIn(0.5f, 3f)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = stringResource(R.string.panorama_zoom_in),
                            tint = Color.White
                        )
                    }

                    // Espacio entre botones
                    Box(modifier = Modifier.size(8.dp))

                    // Botón de zoom out
                    IconButton(
                        onClick = {
                            renderer.zoom = (renderer.zoom / 1.2f).coerceIn(0.5f, 3f)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = stringResource(R.string.panorama_zoom_out),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
