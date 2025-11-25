package com.example.mariamolina.ui.screens.map


import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.data.model.PuntoInteres
import com.example.mariamolina.data.model.SubPuntoInteres
import com.example.mariamolina.data.model.puntosDeInteres
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
    destinoInicial: PuntoInteres? = null,
    subPuntoInicial: SubPuntoInteres? = null, // NUEVO: parámetro para subpuntos
    onNavigateToDetail: (PuntoInteres) -> Unit = {}
) {
    var selectedDestino by remember { mutableStateOf(destinoInicial) }
    var selectedSubPunto by remember { mutableStateOf(subPuntoInicial) } // NUEVO: estado para subpunto
    var showPanel by remember { mutableStateOf(destinoInicial != null || subPuntoInicial != null) } // MODIFICADO
    var drawRoute by remember { mutableStateOf(false) }
    var centerOnRoute by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    val context = LocalContext.current

    // --- 1) CONTROL DE PERMISOS DE UBICACIÓN ---
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        MapViewComposable(
            selectedDestino = selectedDestino,
            selectedSubPunto = selectedSubPunto, // NUEVO: pasar subpunto seleccionado
            drawRoute = drawRoute,
            centerOnRoute = centerOnRoute,
            onFinishCenter = { centerOnRoute = false },
            onMarkerClick = { destino ->
                selectedDestino = destino
                selectedSubPunto = null // NUEVO: limpiar subpunto cuando se selecciona punto principal
                drawRoute = false
                centerOnRoute = false
                showPanel = true
            },
            onSubPuntoMarkerClick = { subPunto -> // NUEVO: callback para subpuntos
                selectedSubPunto = subPunto
                selectedDestino = null // NUEVO: limpiar punto principal cuando se selecciona subpunto
                drawRoute = false
                centerOnRoute = false
                showPanel = true
            },
            onUserLocation = { userLocation = it },
            modifier = Modifier.fillMaxSize()
        )

        // ------- PANEL INFERIOR CUANDO SE PULSA UN DESTINO -------
        AnimatedVisibility(
            visible = showPanel && (selectedDestino != null || selectedSubPunto != null), // MODIFICADO
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // MODIFICADO: Mostrar panel según lo que esté seleccionado
            if (selectedDestino != null) {
                selectedDestino?.let { destino ->
                    DestinoPanel(
                        destino = destino,
                        onNavigate = { onNavigateToDetail(destino) },
                        onShowRoute = {
                            drawRoute = true
                            centerOnRoute = true
                        },
                        onClose = {
                            selectedDestino = null   // Quitar selección del destino
                            selectedSubPunto = null  // NUEVO: limpiar también subpunto
                            drawRoute = false        // Quitar la ruta dibujada
                            showPanel = false        // Ocultar el panel
                        }
                    )
                }
            } else if (selectedSubPunto != null) {
                selectedSubPunto?.let { subPunto ->
                    SubPuntoPanel( // NUEVO: Panel específico para subpuntos
                        subPunto = subPunto,
                        onShowRoute = {
                            drawRoute = true
                            centerOnRoute = true
                        },
                        onClose = {
                            selectedSubPunto = null  // Quitar selección del subpunto
                            selectedDestino = null   // NUEVO: limpiar también punto principal
                            drawRoute = false        // Quitar la ruta dibujada
                            showPanel = false        // Ocultar el panel
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MapViewComposable(
    modifier: Modifier = Modifier,
    selectedDestino: PuntoInteres?,
    selectedSubPunto: SubPuntoInteres?, // NUEVO: parámetro para subpunto seleccionado
    drawRoute: Boolean,
    onMarkerClick: (PuntoInteres) -> Unit,
    onSubPuntoMarkerClick: (SubPuntoInteres) -> Unit, // NUEVO: callback para subpuntos
    centerOnRoute: Boolean,
    onFinishCenter: () -> Unit,
    onUserLocation: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val defaultMarkerDrawable = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)!!
    val selectedMarkerDrawable = defaultMarkerDrawable.constantState?.newDrawable()?.mutate()!!
    val subPuntoMarkerDrawable = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)!!.constantState?.newDrawable()?.mutate()!!

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            runOnFirstFix {
                userLocation = myLocation
            }
        }
    }

    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

    selectedMarkerDrawable.setColorFilter(
        android.graphics.PorterDuffColorFilter(android.graphics.Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)
    )

    // Color diferente para marcadores de subpuntos
    subPuntoMarkerDrawable.setColorFilter(
        android.graphics.PorterDuffColorFilter(android.graphics.Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN)
    )

    AndroidView(
        factory = {
            mapView.apply {
                setMultiTouchControls(true)
                controller.setZoom(16.0)
                controller.setCenter(GeoPoint(41.65213, -4.72856))

                // --- UBICACIÓN DEL USUARIO ---
                locationOverlay.enableMyLocation()

                locationOverlay.runOnFirstFix {
                    val loc = locationOverlay.myLocation
                    if (loc != null) {
                        onUserLocation(loc)
                    }
                }

                //mapView.overlays.add(locationOverlay)
                overlays.add(locationOverlay)
            }
        },
        modifier = modifier,
        update = { map ->

            val userLoc = (map.overlays.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay)?.myLocation
            if (userLoc != null) onUserLocation(userLoc)

            // ----------- 1) MARCADORES DE TODOS LOS DESTINOS PRINCIPALES -----------
            puntosDeInteres.forEach { destino ->
                val marker = Marker(map).apply {
                    position = GeoPoint(destino.latitud, destino.longitud)
                    title = context.getString(destino.tituloResId)
                    icon = if (destino == selectedDestino) selectedMarkerDrawable else defaultMarkerDrawable

                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick(destino)
                        true
                    }
                }
                map.overlays.add(marker)
            }

            // NUEVO: 2) MARCADORES DE SUBPUNTOS (solo si hay un punto principal seleccionado o hay un subpunto inicial)
            if (selectedSubPunto != null) {

                val marker = Marker(map).apply {
                    position = GeoPoint(selectedSubPunto.latitud, selectedSubPunto.longitud)
                    title = context.getString(selectedSubPunto.nombreResId)
                    icon = selectedMarkerDrawable

                    setOnMarkerClickListener { _, _ ->
                        onSubPuntoMarkerClick(selectedSubPunto)
                        true
                    }
                }

                map.overlays.add(marker)
            }

            // ----------- 3) RUTA CUANDO HAY DESTINO SELECCIONADO -----------
            val targetPoint = when {
                selectedDestino != null -> GeoPoint(selectedDestino.latitud, selectedDestino.longitud)
                selectedSubPunto != null -> GeoPoint(selectedSubPunto.latitud, selectedSubPunto.longitud)
                else -> null
            }

            currentPolyline?.let { map.overlays.remove(it) }
            currentPolyline = null

            if (drawRoute && targetPoint != null) {
                val origin = userLocation ?: map.mapCenter as? GeoPoint ?: targetPoint

                val polyline = Polyline().apply {
                    addPoint(origin)
                    addPoint(targetPoint)
                }

                map.overlays.add(polyline)
                currentPolyline = polyline // ✅ guardar la ruta actual
            }

            // ---------- CENTRAR LA RUTA ----------
            if (centerOnRoute && targetPoint != null) {
                val origin = userLocation ?: map.mapCenter as? GeoPoint ?: targetPoint

                // Calculamos el centro medio entre origen y destino
                val middleLat = (origin.latitude + targetPoint.latitude) / 2
                val middleLon = (origin.longitude + targetPoint.longitude) / 2
                val centerPoint = GeoPoint(middleLat, middleLon)

                // Ajustamos zoom para que se vea bien la línea
                map.controller.setZoom(15.0)
                map.controller.animateTo(centerPoint)

                onFinishCenter()   // 👈 IMPORTANTE: reseteamos el estado para no repetirlo
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
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔥 IMAGEN DEL DESTINO
            AsyncImage(
                model = destino.urlImagen,
                contentDescription = stringResource(destino.tituloResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Crop
            )

            // 🔥 TITULO + BOTÓN DE CERRAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(destino.tituloResId),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(Modifier.height(12.dp))

            // 🔥 BOTONES
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

@Composable
fun SubPuntoPanel(
    subPunto: SubPuntoInteres,
    onShowRoute: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔥 TITULO + BOTÓN DE CERRAR (solo título, sin imagen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(subPunto.nombreResId),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(Modifier.height(12.dp))

            // 🔥 SOLO BOTÓN "CÓMO LLEGAR"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = onShowRoute) {
                    Text("Cómo llegar")
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