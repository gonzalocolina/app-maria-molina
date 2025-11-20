package com.example.mariamolina.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.data.model.PuntoInteres
import com.example.mariamolina.data.model.puntosDeInteres
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
    destinoInicial: PuntoInteres? = null,
    onNavigateToDetail: (PuntoInteres) -> Unit = {}
) {
    var selectedDestino by remember { mutableStateOf(destinoInicial) }
    var showPanel by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        MapViewComposable(
            selectedDestino = selectedDestino,
            onMarkerClick = { destino ->
                selectedDestino = destino
                showPanel = true
            },
            modifier = Modifier.fillMaxSize()
        )

        // ------- PANEL INFERIOR CUANDO SE PULSA UN DESTINO -------
        AnimatedVisibility(
            visible = showPanel && selectedDestino != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedDestino?.let { destino ->
                DestinoPanel(
                    destino = destino,
                    onNavigate = { onNavigateToDetail(destino) },
                    onShowRoute = {
                        // solo mostramos la ruta, MapViewComposable la dibuja automáticamente cuando selectedDestino cambia
                    },
                    onClose = { showPanel = false }
                )
            }
        }
    }
}

@Composable
fun MapViewComposable(
    modifier: Modifier = Modifier,
    selectedDestino: PuntoInteres?,
    onMarkerClick: (PuntoInteres) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = {
            mapView.apply {
                setMultiTouchControls(true)
                controller.setZoom(16.0)
                controller.setCenter(GeoPoint(41.65213, -4.72856))
            }
        },
        modifier = modifier,
        update = { map ->

            map.overlays.clear()

            // ----------- 1) MARCADORES DE TODOS LOS DESTINOS -----------
            puntosDeInteres.forEach { destino ->
                val marker = Marker(map).apply {
                    position = GeoPoint(destino.latitud, destino.longitud)
                    title = context.getString(destino.tituloResId)

                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick(destino)
                        true // consumimos el click
                    }
                }
                map.overlays.add(marker)
            }

            // ----------- 2) RUTA CUANDO HAY DESTINO SELECCIONADO -----------
            selectedDestino?.let { destino ->

                val destinoPoint = GeoPoint(destino.latitud, destino.longitud)
                val currentCenter = map.mapCenter as? GeoPoint ?: destinoPoint

                val polyline = Polyline().apply {
                    addPoint(currentCenter)
                    addPoint(destinoPoint)
                }

                map.overlays.add(polyline)

                map.controller.animateTo(destinoPoint, 16.0, 1000)
            }

            map.invalidate()
        }
    )
}

@Composable
fun DestinoPanel(
    destino: PuntoInteres,
    onNavigate: () -> Unit,
    onShowRoute: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(destino.tituloResId),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onShowRoute) {
                    Text("Cómo llegar")
                }

                OutlinedButton(onClick = onNavigate) {
                    Text("Más información")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MariaMolinaTheme {
        MapScreen()
    }
}