package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController // ¡Importante!
import com.example.mariamolina.data.model.puntosDeInteres
import com.example.mariamolina.ui.viewmodel.PointsOfInterestViewModel

@Composable
fun PointsOfInterestScreen(
    // Recibimos el NavHostController para navegar desde aquí
    navController: NavHostController,
    viewModel: PointsOfInterestViewModel
) {
    // Mostramos directamente la lista y delegamos la navegación al NavController principal
    PointsListScreen(
        puntos = puntosDeInteres,
        onPuntoClick = { puntoId ->
            navController.navigate("${com.example.mariamolina.ui.navigation.Pantalla.PointsOfInterest.ruta}/detail/$puntoId")
        },
        viewModel = viewModel
    )
}