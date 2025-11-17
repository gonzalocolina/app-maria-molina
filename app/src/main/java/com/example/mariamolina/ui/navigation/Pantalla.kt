package com.example.mariamolina.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector

// --- ¡Añadimos el parámetro "subtitulo"! ---
sealed class Pantalla(
    val ruta: String,
    val tituloTopBar: String,
    val tituloBottomBar: String,
    val subtitulo: String?, // <-- Puede ser nulo si una vista no tiene
    val icono: ImageVector
) {
    // --- ¡Añadimos los subtítulos a cada objeto! ---
    data object Home : Pantalla(
        ruta = "home",
        tituloTopBar = "Valladolid histórico",
        tituloBottomBar = "Home",
        subtitulo = "Descubre la historia de María de Molina",
        icono = Icons.Default.Home
    )

    data object PointsOfInterest : Pantalla(
        ruta = "pointsOfInterest",
        tituloTopBar = "Puntos interés",
        tituloBottomBar = "Puntos interés",
        subtitulo = "Valladolid Histórico", // <-- ¡Como en tu imagen!
        icono = Icons.Default.LocationOn
    )

    data object Map : Pantalla(
        ruta = "map",
        tituloTopBar = "Mapa",
        tituloBottomBar = "Mapa",
        subtitulo = "Encuentra tu ruta",
        icono = Icons.Default.Map
    )

    data object Kids : Pantalla(
        ruta = "kids",
        tituloTopBar = "Infantil",
        tituloBottomBar = "Infantil",
        subtitulo = "Planes con niños", // (Ejemplo)
        icono = Icons.Default.ChildCare
    )

    data object Profile : Pantalla(
        ruta = "profile",
        tituloTopBar = "Perfil",
        tituloBottomBar = "",
        subtitulo = null,
        icono = Icons.Default.AccountCircle
    )
}

// Lista que usamos en la barra de navegación
val itemsNavegacion = listOf(
    Pantalla.Home,
    Pantalla.PointsOfInterest,
    Pantalla.Map,
    Pantalla.Kids
)