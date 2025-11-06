package com.example.mariamolina.ui.screens.pointsOfInterest

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController // ¡Importante!
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mariamolina.data.model.puntosDeInteresMock

@Composable
fun PointsOfInterestScreen(
    // ¡Asegúrate de que recibe el NavHostController!
    navControllerAnidado: NavHostController
) {
    NavHost(
        navController = navControllerAnidado,
        startDestination = PoiRoutes.LIST // Usa la ruta del archivo nuevo
    ) {
        composable(route = PoiRoutes.LIST) {
            PointsListScreen(
                puntos = puntosDeInteresMock,
                onPuntoClick = { puntoId ->
                    navControllerAnidado.navigate("${PoiRoutes.DETAIL_PREFIX}/$puntoId")
                }
            )
        }
        composable(route = PoiRoutes.DETAIL) { backStackEntry ->
            val puntoId = backStackEntry.arguments?.getString(PoiRoutes.DETAIL_ARG)
            val punto = puntosDeInteresMock.find { it.id == puntoId }
            if (punto != null) {
                PointDetailScreen(punto = punto)
            }
        }
    }
}