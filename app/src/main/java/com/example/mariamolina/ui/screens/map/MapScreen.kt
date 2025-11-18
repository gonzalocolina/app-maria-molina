package com.example.mariamolina.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.data.model.PuntoInteres
import com.example.mariamolina.data.model.puntosDeInteres
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
    onNavigateToDetail: (PuntoInteres) -> Unit = {}   // callback para navegación
) {
    var selectedDestinoForMap by remember { mutableStateOf<PuntoInteres?>(null) }
    var expandedCardId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        MapViewComposable(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            selectedDestino = selectedDestinoForMap
        )

        DestinosList(
            destinos = puntosDeInteres,
            expandedCardId = expandedCardId,
            onExpandToggle = { id ->
                expandedCardId = if (expandedCardId == id) null else id
            },
            onShowOnMap = { destino ->
                selectedDestinoForMap = destino
            },
            onNavigate = onNavigateToDetail,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MapViewComposable(
    modifier: Modifier = Modifier,
    selectedDestino: PuntoInteres?
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
                val destinoPoint = GeoPoint(destino.latitud, destino.longitud)

                val markerInicio = Marker(it).apply {
                    position = startPoint
                    title = "Inicio"
                }
                val markerDestino = Marker(it).apply {
                    position = destinoPoint
                    title = context.getString(destino.tituloResId)
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
    destinos: List<PuntoInteres>,
    expandedCardId: String?,
    onExpandToggle: (String) -> Unit,
    onShowOnMap: (PuntoInteres) -> Unit,
    onNavigate: (PuntoInteres) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(destinos) { destino ->
                DestinoCard(
                    destino = destino,
                    expanded = destino.id == expandedCardId,
                    onExpandToggle = { onExpandToggle(destino.id) },
                    onShowOnMap = { onShowOnMap(destino) },
                    onNavigate = { onNavigate(destino) }
                )
            }
        }
    }
}

@Composable
fun DestinoCard(
    destino: PuntoInteres,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onShowOnMap: () -> Unit,
    onNavigate: () -> Unit
) {
    Card(
        onClick = onExpandToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(modifier = Modifier.fillMaxWidth()) {

            // Título
            Text(
                text = stringResource(id = destino.tituloResId),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            AnimatedVisibility(visible = expanded) {

                Column(modifier = Modifier.padding(16.dp)) {

                    // Imagen desde URL
                    AsyncImage(
                        model = destino.urlImagen,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Botones
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Button(onClick = onShowOnMap) {
                            Text("Ver en el mapa")
                        }

                        OutlinedButton(onClick = onNavigate) {
                            Text("Más información")
                        }
                    }
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