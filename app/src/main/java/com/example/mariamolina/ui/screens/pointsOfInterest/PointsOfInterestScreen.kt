package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController // ¡Importante!
import com.example.mariamolina.data.model.puntosDeInteres
import com.example.mariamolina.ui.navigation.Pantalla
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mariamolina.ui.viewmodel.PointsOfInterestViewModel

@Composable
fun PointsOfInterestScreen(
    // Recibimos el NavHostController para navegar desde aquí
    navController: NavHostController,
    // Recibimos el ViewModel compartido desde AppNavigation
    viewModel: PointsOfInterestViewModel
) {
    val visitados by viewModel.visitados.collectAsState()
    val total = puntosDeInteres.size
    val visitadosCount = visitados.size
    val restantes = total - visitadosCount

    Column {
        Text(text = "Visitados: $visitadosCount, Restantes: $restantes")
        LinearProgressIndicator(
            progress = { if (total > 0) visitadosCount.toFloat() / total else 0f },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        // Mostramos directamente la lista y delegamos la navegación al NavController principal
        PointsListScreen(
            puntos = puntosDeInteres,
            onPuntoClick = { puntoId ->
                navController.navigate("${Pantalla.PointsOfInterest.ruta}/detail/$puntoId")
            }
        )
    }
}