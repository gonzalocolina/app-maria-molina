package com.example.mariamolina.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

// Modelo de destino
data class Destino(val nombre: String, val lat: Double, val lon: Double)

// Lista de destinos de ejemplo
val destinos = listOf(
    Destino("Monasterio de las Huelgas Reales", 41.6542, -4.7165),
    Destino("Monasterio de Santa María de Palazuelos", 41.7527, -4.63383),
    Destino("Meneses de Campos", 41.94296, -4.92032),
    Destino("Montealegre", 41.90226, -4.8998)
)

@Composable
fun MapScreen() {
    var selectedDestino by remember { mutableStateOf<Destino?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Mapa (arriba)
        MapViewComposable(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            selectedDestino = selectedDestino
        )

        // Lista (abajo)
        DestinosList(
            destinos = destinos,
            onDestinoClick = { destino -> selectedDestino = destino },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MapViewComposable(
    modifier: Modifier = Modifier,
    selectedDestino: Destino?
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = {
            mapView.apply {
                setMultiTouchControls(true)
                val startPoint = GeoPoint(41.65213, -4.72856)
                controller.setZoom(15.0)
                controller.setCenter(startPoint)

                // Marcador inicial
                val marker = Marker(this)
                marker.position = startPoint
                marker.title = "Valladolid"
                overlays.add(marker)
            }
        },
        modifier = modifier,
        update = {
            selectedDestino?.let { destino ->
                it.overlays.clear()

                val startPoint = GeoPoint(41.65213, -4.72856)
                val destinoPoint = GeoPoint(destino.lat, destino.lon)

                val markerInicio = Marker(it).apply {
                    position = startPoint
                    title = "Inicio"
                }
                val markerDestino = Marker(it).apply {
                    position = destinoPoint
                    title = destino.nombre
                }

                val line = Polyline().apply {
                    addPoint(startPoint)
                    addPoint(destinoPoint)
                }

                it.overlays.addAll(listOf(markerInicio, markerDestino, line))

                // Encuadrar ambos puntos en la pantalla
                val boundingBox = org.osmdroid.util.BoundingBox(
                    maxOf(startPoint.latitude, destinoPoint.latitude),
                    maxOf(startPoint.longitude, destinoPoint.longitude),
                    minOf(startPoint.latitude, destinoPoint.latitude),
                    minOf(startPoint.longitude, destinoPoint.longitude)
                )

                // Expandir el bounding box para que haya más zoom-out
                val expandFactor = 1.4
                val expandedBox = org.osmdroid.util.BoundingBox(
                    boundingBox.latNorth + (boundingBox.latNorth - boundingBox.latSouth) * (expandFactor - 1f),
                    boundingBox.lonEast + (boundingBox.lonEast - boundingBox.lonWest) * (expandFactor - 1f),
                    boundingBox.latSouth - (boundingBox.latNorth - boundingBox.latSouth) * (expandFactor - 1f),
                    boundingBox.lonWest - (boundingBox.lonEast - boundingBox.lonWest) * (expandFactor - 1f)
                )

                // Aplicar zoom-out
                it.zoomToBoundingBox(expandedBox, true)
                it.invalidate()
            }
        }
    )
}

@Composable
fun DestinosList(
    destinos: List<Destino>,
    onDestinoClick: (Destino) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(destinos) { destino ->
                DestinoCard(destino = destino, onClick = { onDestinoClick(destino) })
            }
        }
    }
}

@Composable
fun DestinoCard(destino: Destino, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = destino.nombre,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MariaMolinaTheme {
        MapScreen()
    }
}