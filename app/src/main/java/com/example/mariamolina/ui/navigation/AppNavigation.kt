package com.example.mariamolina.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.ui.screens.home.HomeScreen
import com.example.mariamolina.ui.screens.kids.KidsScreen
import com.example.mariamolina.ui.screens.map.MapScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PointsOfInterestScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                itemsNavegacion.forEach { pantalla ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == pantalla.ruta } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(pantalla.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(pantalla.titulo) },
                        icon = { Icon(pantalla.icono, contentDescription = pantalla.titulo) },

                        // 3. PERSONALIZACIÓN DE COLORES
                        colors = NavigationBarItemDefaults.colors(
                            // Color del icono cuando está seleccionado (puedes cambiarlo)
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            // Color del icono cuando NO está seleccionado
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            // Color del texto cuando está seleccionado
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            // Color del texto cuando NO está seleccionado
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            // 4. CAMBIO AQUÍ:
                            // Le damos color al indicador por defecto (forma de píldora)
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Pantalla.Home.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 5. Actualizamos el NavHost con las 4 pantallas
            composable(Pantalla.Home.ruta) { HomeScreen() }
            composable(Pantalla.PointsOfInterest.ruta) { PointsOfInterestScreen() }
            composable(Pantalla.Map.ruta) { MapScreen() }
            composable(Pantalla.Kids.ruta) { KidsScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    MariaMolinaTheme {
        AppNavigation()
    }
}
