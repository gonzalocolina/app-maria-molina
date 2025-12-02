package com.example.mariamolina.ui.screens.map

import android.content.pm.PackageManager
import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
    destinoInicial: PuntoInteres? = null,
    subPuntoInicial: SubPuntoInteres? = null,
    onNavigateToDetail: (PuntoInteres) -> Unit = {}
) {
    var selectedDestino by remember { mutableStateOf(destinoInicial) }
    var selectedSubPunto by remember { mutableStateOf(subPuntoInicial) }
    var showPanel by remember { mutableStateOf(destinoInicial != null || subPuntoInicial != null) }
    var drawRoute by remember { mutableStateOf(false) }
    var centerOnRoute by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var centerMapOnUser by remember { mutableStateOf(false) }
    var centerMapOnInitial by remember { mutableStateOf(false) }
    var isCalculatingRoute by remember { mutableStateOf(false) }
    var showPanelImage by remember { mutableStateOf(true) }
    var centerMapOnDestino by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Control de permisos de ubicación
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

    // Este es el LaunchedEffect que reinicia la imagen al cambiar de panel
    LaunchedEffect(selectedDestino, selectedSubPunto) {
        if (selectedDestino != null || selectedSubPunto != null) {
            showPanelImage = true
        }
    }

    LaunchedEffect(destinoInicial, subPuntoInicial) {
        if (destinoInicial != null || subPuntoInicial != null) {
            // Espera un momento para asegurar que el mapa esté listo
            kotlinx.coroutines.delay(100)
            centerMapOnDestino = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        MapViewComposable(
            selectedDestino = selectedDestino,
            selectedSubPunto = selectedSubPunto,
            drawRoute = drawRoute,
            centerOnRoute = centerOnRoute,
            centerMapOnUser = centerMapOnUser,
            centerMapOnInitial = centerMapOnInitial,
            centerMapOnDestino = centerMapOnDestino,
            onFinishCenterUser = { centerMapOnUser = false },
            onFinishCenterInitial = { centerMapOnInitial = false },
            onFinishCenterDestino = { centerMapOnDestino = false },
            onFinishCenter = { centerOnRoute = false },
            onMarkerClick = { destino ->
                selectedDestino = destino
                selectedSubPunto = null
                drawRoute = false
                centerOnRoute = false
                showPanel = true
            },
            onSubPuntoMarkerClick = { subPunto ->
                selectedSubPunto = subPunto
                selectedDestino = null
                drawRoute = false
                centerOnRoute = false
                showPanel = true
            },
            onUserLocation = { userLocation = it },
            isCalculatingRoute = isCalculatingRoute,
            onRouteCalculationStart = { isCalculatingRoute = true },
            onRouteCalculationEnd = { isCalculatingRoute = false },
            onMapTap = {
                if (showPanel) {
                    showPanelImage = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (userLocation != null) {
            FloatingActionButton(
                onClick = { centerMapOnUser = true },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text("Mi ubicación")
            }
        }

        FloatingActionButton(
            onClick = { centerMapOnInitial = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Text("Inicio")
        }

        // Indicador de carga para la ruta
        if (isCalculatingRoute) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Calculando ruta...")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showPanel && (selectedDestino != null || selectedSubPunto != null),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (selectedDestino != null) {
                selectedDestino?.let { destino ->
                    DestinoPanel(
                        destino = destino,
                        onNavigate = { onNavigateToDetail(destino) },
                        onShowRoute = {
                            if(!drawRoute){
                                drawRoute = true
                            }
                            centerOnRoute = true
                        },
                        onClose = {
                            selectedDestino = null
                            selectedSubPunto = null
                            drawRoute = false
                            showPanel = false
                        },
                        isCalculatingRoute = isCalculatingRoute,
                        showImage = showPanelImage,
                        onPanelTapped = { showPanelImage = true }
                    )
                }
            } else if (selectedSubPunto != null) {
                selectedSubPunto?.let { subPunto ->
                    SubPuntoPanel(
                        subPunto = subPunto,
                        onShowRoute = {
                            if(!drawRoute){
                                drawRoute = true
                            }
                            centerOnRoute = true
                        },
                        onClose = {
                            selectedSubPunto = null
                            selectedDestino = null
                            drawRoute = false
                            showPanel = false
                        },
                        isCalculatingRoute = isCalculatingRoute
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
    selectedSubPunto: SubPuntoInteres?,
    drawRoute: Boolean,
    onMarkerClick: (PuntoInteres) -> Unit,
    onSubPuntoMarkerClick: (SubPuntoInteres) -> Unit,
    centerOnRoute: Boolean,
    centerMapOnUser: Boolean,
    centerMapOnInitial: Boolean,
    centerMapOnDestino: Boolean,
    onFinishCenterDestino: () -> Unit,
    onFinishCenterUser: () -> Unit,
    onFinishCenterInitial: () -> Unit,
    onFinishCenter: () -> Unit,
    onUserLocation: (GeoPoint) -> Unit,
    isCalculatingRoute: Boolean,
    onRouteCalculationStart: () -> Unit,
    onRouteCalculationEnd: () -> Unit,
    onMapTap: () -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val defaultMarkerDrawable = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)!!
    val selectedMarkerDrawable = defaultMarkerDrawable.constantState?.newDrawable()?.mutate()!!
    val subPuntoMarkerDrawable = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)!!.constantState?.newDrawable()?.mutate()!!

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var currentRoutePolyline by remember { mutableStateOf<Polyline?>(null) }

    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            runOnFirstFix {
                userLocation = myLocation
                onUserLocation(myLocation)
            }
        }
    }

    selectedMarkerDrawable.setColorFilter(
        android.graphics.PorterDuffColorFilter(android.graphics.Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)
    )

    subPuntoMarkerDrawable.setColorFilter(
        android.graphics.PorterDuffColorFilter(android.graphics.Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN)
    )

    LaunchedEffect(centerOnRoute, currentRoutePolyline) {
        if (centerOnRoute && currentRoutePolyline != null) {
            // Centrar en la ruta existente sin recalcular
            val boundingBox = currentRoutePolyline!!.bounds
            mapView.zoomToBoundingBox(boundingBox, true, 150)
            onFinishCenter()
        }
    }

    LaunchedEffect(centerMapOnDestino, selectedDestino, selectedSubPunto) {
        if (centerMapOnDestino) {
            val pointToCenter = when {
                selectedDestino != null -> GeoPoint(selectedDestino.latitud, selectedDestino.longitud)
                selectedSubPunto != null -> GeoPoint(selectedSubPunto.latitud, selectedSubPunto.longitud)
                else -> null
            }

            if (pointToCenter != null) {
                mapView.controller.animateTo(pointToCenter)
                onFinishCenterDestino()
            }
        }
    }

    // Efecto para manejar el cálculo y dibujo de rutas
    LaunchedEffect(drawRoute, selectedDestino, selectedSubPunto, userLocation) {
        if (drawRoute && userLocation != null && !isCalculatingRoute) {
            val targetPoint = when {
                selectedDestino != null -> GeoPoint(selectedDestino.latitud, selectedDestino.longitud)
                selectedSubPunto != null -> GeoPoint(selectedSubPunto.latitud, selectedSubPunto.longitud)
                else -> null
            }

            if (targetPoint != null) {
                onRouteCalculationStart()

                // Limpiar ruta anterior
                currentRoutePolyline?.let { polyline ->
                    mapView.overlays.remove(polyline)
                }

                try {
                    // Calcular ruta real con OSRM
                    val routePoints = getRouteFromOSRM(userLocation!!, targetPoint)

                    if (routePoints.isNotEmpty()) {
                        val routePolyline = Polyline().apply {
                            setPoints(routePoints)
                            setColor(0xAA0066CC.toInt())
                            setWidth(14.0f)
                        }
                        mapView.overlays.add(routePolyline)
                        currentRoutePolyline = routePolyline

                        // Centrar en la ruta si es necesario
                        if (centerOnRoute) {
                            val boundingBox = routePolyline.bounds
                            mapView.zoomToBoundingBox(boundingBox, true, 150)
                            onFinishCenter()
                        }
                    } else {
                        // Fallback a línea recta si no se pudo obtener ruta
                        createFallbackRoute(mapView, userLocation!!, targetPoint)
                    }
                } catch (e: Exception) {
                    // Fallback a línea recta en caso de error
                    createFallbackRoute(mapView, userLocation!!, targetPoint)
                }

                onRouteCalculationEnd()
                mapView.invalidate()
            }
        } else if (!drawRoute) {
            // Limpiar ruta cuando se desactiva drawRoute
            currentRoutePolyline?.let { polyline ->
                mapView.overlays.remove(polyline)
                currentRoutePolyline = null
                mapView.invalidate()
            }
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                setMultiTouchControls(true)
                controller.setZoom(16.0)
                controller.setCenter(GeoPoint(41.65213, -4.72856))

                locationOverlay.enableMyLocation()
                overlays.add(locationOverlay)
            }
        },
        modifier = modifier,
        update = { map ->
            val userLoc = locationOverlay.myLocation
            if (userLoc != null && userLocation == null) {
                userLocation = userLoc
                onUserLocation(userLoc)
            }

            // Limpiar solo marcadores, mantener ubicación y rutas
            val nonMarkerOverlays = map.overlays.filter {
                it !is Marker && it !is MyLocationNewOverlay
            }
            map.overlays.clear()
            map.overlays.addAll(nonMarkerOverlays)

            // Asegurar que la ubicación esté presente
            if (!map.overlays.contains(locationOverlay)) {
                map.overlays.add(locationOverlay)
            }

            // Asegurar que la ruta actual esté presente
            currentRoutePolyline?.let { route ->
                if (!map.overlays.contains(route)) {
                    map.overlays.add(route)
                }
            }

            map.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    onMapTap()
                }
                false
            }

            // Marcadores de destinos principales
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

            // Marcadores de subpuntos
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

            // Controles de centrado
            if (centerMapOnUser && userLocation != null) {
                map.controller.setZoom(16.0)
                map.controller.animateTo(userLocation)
                onFinishCenterUser()
            }

            if (centerMapOnInitial) {
                val initialPoint = GeoPoint(41.65213, -4.72856)
                map.controller.setZoom(16.0)
                map.controller.animateTo(initialPoint)
                onFinishCenterInitial()
            }

            // Centrar en el destino inicial cuando se navega desde "Ver en Mapa"
            if (centerMapOnDestino) {
                val pointToCenter = when {
                    selectedDestino != null -> GeoPoint(selectedDestino.latitud, selectedDestino.longitud)
                    selectedSubPunto != null -> GeoPoint(selectedSubPunto.latitud, selectedSubPunto.longitud)
                    else -> null
                }

                if (pointToCenter != null) {
                    map.controller.animateTo(pointToCenter)
                    onFinishCenterDestino()
                }
            }

            map.invalidate()
        }
    )
}

// Función para obtener ruta desde OSRM
private suspend fun getRouteFromOSRM(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://router.project-osrm.org/route/v1/driving/" +
                    "${start.longitude},${start.latitude};${end.longitude},${end.latitude}?" +
                    "overview=full&geometries=geojson"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "MariamolinaApp/1.0")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseOSRMResponse(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// Función para parsear la respuesta de OSRM
private fun parseOSRMResponse(jsonResponse: String): List<GeoPoint> {
    return try {
        val jsonObject = JSONObject(jsonResponse)
        val routes = jsonObject.getJSONArray("routes")
        if (routes.length() > 0) {
            val route = routes.getJSONObject(0)
            val geometry = route.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")

            val points = mutableListOf<GeoPoint>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                val longitude = coord.getDouble(0)
                val latitude = coord.getDouble(1)
                points.add(GeoPoint(latitude, longitude))
            }
            points
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// Función de fallback para línea recta
private fun createFallbackRoute(mapView: MapView, start: GeoPoint, end: GeoPoint): Polyline {
    val fallbackPolyline = Polyline().apply {
        addPoint(start)
        addPoint(end)
        setColor(0xAAFF6600.toInt()) // Naranja para indicar fallback
        setWidth(10.0f)
    }
    mapView.overlays.add(fallbackPolyline)
    return fallbackPolyline
}

@Composable
fun DestinoPanel(
    destino: PuntoInteres,
    onNavigate: () -> Unit,
    onShowRoute: () -> Unit,
    onClose: () -> Unit,
    isCalculatingRoute: Boolean = false,
    showImage: Boolean = true,
    onPanelTapped: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val imageHeight = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 100.dp else 150.dp
    val maxWidth = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 420.dp else 600.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Alignment.CenterStart else Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .padding(16.dp)
                .clickable { onPanelTapped() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                if (showImage) {
                    AsyncImage(
                        model = destino.urlImagen,
                        contentDescription = stringResource(destino.tituloResId),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onShowRoute,
                        enabled = !isCalculatingRoute
                    ) {
                        if (isCalculatingRoute) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Calculando...")
                        } else {
                            Text("Cómo llegar")
                        }
                    }

                    OutlinedButton(onClick = onNavigate) {
                        Text("Más información")
                    }
                }
            }
        }
    }
}

@Composable
fun SubPuntoPanel(
    subPunto: SubPuntoInteres,
    onShowRoute: () -> Unit,
    onClose: () -> Unit,
    isCalculatingRoute: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val maxWidth = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 420.dp else 600.dp
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Alignment.CenterStart else Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onShowRoute,
                        enabled = !isCalculatingRoute
                    ) {
                        if (isCalculatingRoute) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Calculando...")
                        } else {
                            Text("Cómo llegar")
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