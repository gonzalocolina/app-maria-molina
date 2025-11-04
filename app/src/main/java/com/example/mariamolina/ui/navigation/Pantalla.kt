package com.example.mariamolina.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

// 1. Actualizamos la sealed class con las 4 pantallas que quieres
sealed class Pantalla(val ruta: String, val titulo: String, val icono: ImageVector) {
    object Home : Pantalla("home", "Inicio", Icons.Default.Home)
    object PointsOfInterest : Pantalla("poi", "Puntos de interés", Icons.Default.Place)
    object Map : Pantalla("map", "Mapa", Icons.Default.Map)
    object Kids : Pantalla("kids", "Infantil", Icons.Default.ChildCare)
}

// 2. Actualizamos la lista de items para la barra
val itemsNavegacion = listOf(
    Pantalla.Home,
    Pantalla.PointsOfInterest,
    Pantalla.Map,
    Pantalla.Kids,
)