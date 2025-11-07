package com.example.mariamolina.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Nueva pantalla de "Mapa"

@Composable
fun MapScreen() {
    //Box(
    //    modifier = Modifier.fillMaxSize(),
    //    contentAlignment = Alignment.Center
    //) {
    //    Text(text = "Vista de Mapa", style = MaterialTheme.typography.headlineMedium)
    //}
    AndroidView(factory = { context ->
        val map = MapView(context)
        map.setMultiTouchControls(true)

        // Centro del mapa (Madrid)
        val startPoint = GeoPoint(40.4168, -3.7038)
        map.controller.setZoom(14.0)
        map.controller.setCenter(startPoint)

        // Añadir marcador
        val marker = Marker(map)
        marker.position = startPoint
        marker.title = "Madrid, España"
        map.overlays.add(marker)

        map
    })
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MariaMolinaTheme {
        MapScreen()
    }
}