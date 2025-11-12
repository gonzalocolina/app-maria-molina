package com.example.mariamolina.ui.navigation

import androidx.compose.foundation.layout.Column // ¡Importado!
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons // ¡Importado!
import androidx.compose.material.icons.filled.AccountCircle // ¡Importado!
import androidx.compose.material3.ExperimentalMaterial3Api // ¡Importado!
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // ¡Importado!
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar // ¡Importado!
import androidx.compose.material3.TopAppBarDefaults // ¡Importado!
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow // ¡Importado!
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController // ¡CAMBIO! Importación añadida
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mariamolina.ui.theme.MariaMolinaTheme
import com.example.mariamolina.ui.screens.home.HomeScreen
import com.example.mariamolina.ui.screens.home.ImageScreen
import com.example.mariamolina.ui.screens.kids.KidsScreen
import com.example.mariamolina.ui.screens.map.MapScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PointsOfInterestScreen
import com.example.mariamolina.ui.screens.pointsOfInterest.PoiRoutes // ¡CAMBIO! Importación añadida


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navControllerPrincipal = rememberNavController()
    val navControllerAnidadoPoi = rememberNavController()

    // 1. Escuchamos al controlador principal (para títulos y barra inferior)
    val navBackStackEntry by navControllerPrincipal.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = itemsNavegacion.find { it.ruta == currentDestination?.route }
        ?: Pantalla.Home

    // 2. ¡CAMBIO! Escuchamos al controlador ANIDADO (para saber si ocultar la TopBar)
    val poiNavBackStackEntry by navControllerAnidadoPoi.currentBackStackEntryAsState()
    val poiCurrentRoute = poiNavBackStackEntry?.destination?.route


    // 2. Sólo escuchamos al controlador ANIDADO cuando la pestaña activa es PointsOfInterest
//    val poiCurrentRoute = if (currentScreen == Pantalla.PointsOfInterest) {
//        val poiNavBackStackEntry by navControllerAnidadoPoi.currentBackStackEntryAsState()
//        poiNavBackStackEntry?.destination?.route
//    } else {
//        null
//    }

    Scaffold(
        topBar = {
            // 3. ¡CAMBIO! Lógica condicional para mostrar la barra
            // Comprobamos si la ruta anidada actual EMPIEZA CON "poi_detail"
            val esRutaDetalle = poiCurrentRoute?.startsWith(PoiRoutes.DETAIL_PREFIX) == true

            // Solo mostramos la barra si NO estamos en el detalle
            if (!esRutaDetalle) {
                AppTopBar(
                    titulo = currentScreen.tituloTopBar,
                    subtitulo = currentScreen.subtitulo
                )
            }
        },
        bottomBar = {
            NavigationBar {
                itemsNavegacion.forEach { pantalla ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.route == pantalla.ruta } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navControllerPrincipal.navigate(pantalla.ruta) {
                                popUpTo(navControllerPrincipal.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(pantalla.tituloBottomBar) },
                        icon = { Icon(pantalla.icono, contentDescription = pantalla.tituloBottomBar) },
                        //alwaysShowLabel = false, // Lo tenías comentado, lo dejo así
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navControllerPrincipal,
            startDestination = Pantalla.Home.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Pantalla.Home.ruta) {
                HomeScreen(onNavigateToImage = { navControllerPrincipal.navigate("image") })
            }
            // nueva ruta para la pantalla de imagen
            composable("image") {
                ImageScreen(onBackClick = { navControllerPrincipal.popBackStack() })
            }


            // 4. ¡CAMBIO! Pasamos el controlador anidado a la pantalla de POI
            composable(Pantalla.PointsOfInterest.ruta) {
                PointsOfInterestScreen(navControllerAnidado = navControllerAnidadoPoi)
            }

            composable(Pantalla.Map.ruta) { MapScreen() }
            composable(Pantalla.Kids.ruta) { KidsScreen() }
        }
    }
}

// (Tu función AppTopBar y el Preview quedan exactamente igual)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(titulo: String, subtitulo: String?) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            IconButton(onClick = { /* TODO: Aquí iría la navegación a Perfil */ }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    tint = Color(0xFFF5E6D3)
                )
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    MariaMolinaTheme {
        AppNavigation()
    }
}