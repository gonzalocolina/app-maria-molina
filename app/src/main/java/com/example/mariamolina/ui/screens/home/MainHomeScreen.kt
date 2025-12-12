package com.example.mariamolina.ui.screens.home

import androidx.compose.runtime.Composable
import com.example.mariamolina.ui.screens.home.HomeScreen

@Composable
fun MainScreen() {
    // Este archivo anteriormente contenía un NavHost anidado. No lo necesitamos
    // porque ahora `AppNavigation` es el NavHost principal.
    // Dejamos aquí una función simple que renderiza `HomeScreen` para previews o uso directo.
    HomeScreen(onNavigateToImage = {})
}
