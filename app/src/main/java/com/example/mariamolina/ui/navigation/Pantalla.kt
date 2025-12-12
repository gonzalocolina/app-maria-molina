package com.example.mariamolina.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mariamolina.R

// --- ¡Añadimos el parámetro "subtitulo"! ---
sealed class Pantalla(
    val ruta: String,
    val tituloTopBarResId: Int,
    val tituloBottomBarResId: Int,
    val subtituloResId: Int?, // <-- Puede ser nulo si una vista no tiene
    val icono: ImageVector
) {
    // --- ¡Añadimos los subtítulos a cada objeto! ---
    data object Home : Pantalla(
        ruta = "home",
        tituloTopBarResId = R.string.nav_home_title,
        tituloBottomBarResId = R.string.nav_home_bottom,
        subtituloResId = R.string.nav_home_subtitle,
        icono = Icons.Default.Home
    )

    data object PointsOfInterest : Pantalla(
        ruta = "pointsOfInterest",
        tituloTopBarResId = R.string.nav_points_title,
        tituloBottomBarResId = R.string.nav_points_bottom,
        subtituloResId = R.string.nav_points_subtitle, // <-- ¡Como en tu imagen!
        icono = Icons.Default.LocationOn
    )

    data object Map : Pantalla(
        ruta = "map",
        tituloTopBarResId = R.string.nav_map_title,
        tituloBottomBarResId = R.string.nav_map_bottom,
        subtituloResId = R.string.nav_map_subtitle,
        icono = Icons.Default.Map
    )

    data object Kids : Pantalla(
        ruta = "kids",
        tituloTopBarResId = R.string.nav_kids_title,
        tituloBottomBarResId = R.string.nav_kids_bottom,
        subtituloResId = R.string.nav_kids_subtitle, // (Ejemplo)
        icono = Icons.Default.ChildCare
    )

    data object Profile : Pantalla(
        ruta = "profile",
        tituloTopBarResId = R.string.nav_profile_title,
        tituloBottomBarResId = R.string.nav_home_bottom, // empty
        subtituloResId = null,
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